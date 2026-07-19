package com.sharkdom.service.stripe;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.OrgUserMappingStatus;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.entity.stripe.StripeCustomer;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.mapper.stripe.StripeCustomerMapper;
import com.sharkdom.model.stripe.StripeCustomerDto;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.stripe.StripeCustomerRepository;
import com.sharkdom.repository.user.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeCustomerServiceImpl implements StripeCustomerService {

    @Resource
    StripeCustomerServiceImpl stripeCustomerService;

    private final StripeCustomerRepository stripeCustomerRepository;

    private final StripeCustomerMapper stripeCustomerMapper;

    private final StripeService stripeService;

    private final OrganizationUserMappingRepository organizationUserMappingRepository;

    private final UserRepository userRepository;

    @Transactional
    public StripeCustomerDto createCustomer(StripeCustomerDto stripeCustomerDto) throws StripeException {
        log.info("Creating customer with details: {}", stripeCustomerDto);
        try {
            if (stripeCustomerRepository.existsByCustomerId(stripeCustomerDto.getCustomerId())) {
                return stripeCustomerService.getCustomerByCustomerId(stripeCustomerDto.getCustomerId());
            }
            if (!ObjectUtils.isEmpty(stripeCustomerDto.getFirebaseUserId()) && ObjectUtils.isEmpty(stripeCustomerDto.getOrganizationId())) {
                List<OrganizationUserMapping> allUsersByOrganizationId = organizationUserMappingRepository.findAllByUserId(stripeCustomerDto.getFirebaseUserId());
                Set<Long> organizationIds = allUsersByOrganizationId.stream().map(OrganizationUserMapping::getOrganizationId).collect(Collectors.toSet());
                stripeCustomerDto.setOrganizationId(organizationIds);
            } else if (!ObjectUtils.isEmpty(stripeCustomerDto.getOrganizationId()) && ObjectUtils.isEmpty(stripeCustomerDto.getFirebaseUserId())) {
                String foundFirebaseUserId = stripeCustomerDto.getOrganizationId().stream()
                        .map(organizationId -> organizationUserMappingRepository.findByOrganizationId(organizationId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH46, organizationId)))
                        .filter(mapping -> OrgUserMappingStatus.ACTIVE.equals(mapping.getStatus()))
                        .map(OrganizationUserMapping::getUserId)
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH47, stripeCustomerDto.getOrganizationId()));
                stripeCustomerDto.setFirebaseUserId(foundFirebaseUserId);
            }

            Customer customer = stripeService.createStripeCustomer(stripeCustomerDto);
            log.info("Customer created successfully. Customer : {}", customer.getId());
            stripeCustomerDto.setCustomerId(customer.getId());
            StripeCustomer toSaveCustomer = stripeCustomerMapper.customerDtoToCustomer(stripeCustomerDto);
            StripeCustomer savedCustomer = stripeCustomerRepository.save(toSaveCustomer);
            log.info("Customer saved to database successfully. {}", savedCustomer);
            return stripeCustomerMapper.customerToCustomerDto(savedCustomer);
        } catch (StripeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Something went wrong while creating Customer: {}", e.getMessage());
            throw new ServiceException(ErrorMessages.SH149, "creating", e.getMessage());
        }
    }


    @NotNull
    public StripeCustomerDto getStripeCustomerDtoByUserId(StripeCustomerDto customerDto) {
        if (stripeCustomerRepository.existsByFirebaseUserId(customerDto.getFirebaseUserId())) {
            StripeCustomer byFirebaseUserId = stripeCustomerRepository.findByFirebaseUserId(customerDto.getFirebaseUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH48, customerDto.getFirebaseUserId()));
            return stripeCustomerMapper.customerToCustomerDto(byFirebaseUserId);
        }
        User byUserId = userRepository.findByUserId(customerDto.getFirebaseUserId())
                .orElseThrow(() -> new SharkdomException(ErrorMessages.SH78, customerDto.getFirebaseUserId()));
        customerDto.setCustomerEmail(byUserId.getEmail());
        customerDto.setCustomerName(byUserId.getName());
        return customerDto;
    }

    @NotNull
    public StripeCustomerDto getStripeCustomerDtoByUserId(String userId) {
        if (stripeCustomerRepository.existsByFirebaseUserId(userId)) {
            StripeCustomer stripeCustomer = stripeCustomerRepository
                    .findByFirebaseUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH48, userId));

            return stripeCustomerMapper.customerToCustomerDto(stripeCustomer);
        }
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new SharkdomException(ErrorMessages.SH78, userId));
        StripeCustomerDto customerDto = new StripeCustomerDto();
        customerDto.setFirebaseUserId(userId);
        customerDto.setCustomerEmail(user.getEmail());
        customerDto.setCustomerName(user.getName());
        return customerDto;
    }

    @Transactional
    @Override
    public List<StripeCustomerDto> getAllCustomers() {
        List<StripeCustomerDto> stripeCustomerDtoList;
        try {
            stripeCustomerDtoList = stripeCustomerMapper.customerListToCustomerDtoList(stripeCustomerRepository.findAll());
        } catch (Exception e) {
            log.error("Customers not found. \n Error : {}", e.getMessage());
            throw e;
        }
        return stripeCustomerDtoList;
    }

    @Transactional
    @Override
    public StripeCustomerDto getCustomerByCustomerId(String customerId) {
        StripeCustomer stripeCustomer = stripeCustomerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new SharkdomException(ErrorMessages.SH112, customerId));
        return stripeCustomerMapper.customerToCustomerDto(stripeCustomer);
    }

}
