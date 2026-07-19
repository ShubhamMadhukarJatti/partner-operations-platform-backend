package com.sharkdom.deals.service;

import com.razorpay.RazorpayClient;
import com.razorpay.Transfer;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.constants.user.ApprovalRequestHistoryStatus;
import com.sharkdom.deals.dto.CapturedPaymentResponse;
import com.sharkdom.deals.dto.CommissionRequestRequest;
import com.sharkdom.deals.dto.CommissionRequestResponse;
import com.sharkdom.deals.entity.*;
import com.sharkdom.deals.enums.CommissionAction;
import com.sharkdom.deals.model.*;
import com.sharkdom.deals.repository.*;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.paymenttracking.RazorpayTrackingEntity;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.paymenttracking.RazorpayTrackingRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DealsService {
    @Value("${api-url}")
    private String apiUrl;
    @Value("${razorPay.keyId}")
    String razorPayKeyId;
    @Value("${razorPay.keySecret}")
    String razorPayKeySecret;
    private final DealsRepository dealsRepository;
    private final DealsJoinerRepository dealsJoinerRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final OrganizationRepository organizationRepository;
    private final IntegrationRepository integrationRepository;
    private RazorpayClient razorpayClient;
    private final CommissionRequestRepository commissionRequestRepository;
    private final RazorpayTrackingRepository razorpayTrackingRepository;


    public DealsService(DealsRepository dealsRepository, DealsJoinerRepository dealsJoinerRepository, WalletTransactionRepository walletTransactionRepository, WalletRepository walletRepository, OrganizationRepository organizationRepository, IntegrationRepository integrationRepository, CommissionRequestRepository commissionRequestRepository, RazorpayTrackingRepository razorpayTrackingRepository) {
        this.dealsRepository = dealsRepository;
        this.dealsJoinerRepository = dealsJoinerRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletRepository = walletRepository;
        this.organizationRepository = organizationRepository;
        this.integrationRepository = integrationRepository;
        this.commissionRequestRepository = commissionRequestRepository;
        this.razorpayTrackingRepository = razorpayTrackingRepository;
    }

    public DealsEntity createDeal(CreateDealRequest createDealRequest) {
        String dealId = generateUniqueDealId();
        DealsEntity dealsEntity = DealsEntity.builder()
                .dealId(dealId)
                .organizationId(createDealRequest.organizationId())
                .offerDetail(createDealRequest.offerDetail())
                .commission(createDealRequest.commission())
                .restrictedSectors(createDealRequest.restrictedSectors())
                .channelAllowed(createDealRequest.channelAllowed())
                .quotaRemaining(createDealRequest.quotaRemaining())
                .geography(createDealRequest.geography())
                .approvalRequired(createDealRequest.approvalRequired())
                .status(createDealRequest.status())
                .pageURL(createDealRequest.pageURL())
                .build();

        return dealsRepository.save(dealsEntity);
    }

    private String generateUniqueDealId() {
        SecureRandom random = new SecureRandom();
        StringBuilder dealIdBuilder = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(characters.length());
            dealIdBuilder.append(characters.charAt(index));
        }
        return dealIdBuilder.toString();
    }

    public List<DealsResponse> getMyDeals(Long organizationId, DealType dealType) {
        if (dealType.equals(DealType.CREATED)) {
            var createdDeals = dealsRepository.findAllByOrganizationId(organizationId);
            return createdDeals.stream()
                    .map(deal -> {
                        var logoUrl = organizationRepository.findLogoUrlById(deal.getOrganizationId());
                        return mapToDealsResponse(deal, logoUrl);
                    })
                    .toList();
        } else {
            var dealsJoiner = dealsJoinerRepository.findAllByOrganizationId(organizationId);
            Set<String> joinedDealIds = dealsJoiner.stream()
                    .map(DealsJoinerEntity::getDealId)
                    .collect(Collectors.toSet());
            List<DealsEntity> joinedDealDetails = dealsRepository.findAllByDealIdIn(joinedDealIds);


            // Map joined deals with "Joined" label
            return joinedDealDetails.stream()
                    .map(deal -> {
                        var logoUrl = organizationRepository.findLogoUrlById(deal.getOrganizationId());
                        return mapToDealsResponse(deal, logoUrl);
                    })
                    .toList();

        }
    }

    public List<DealsResponse> getAllDeals(Long organizationId) {
        var deals = dealsRepository.findAllExceptMyDeals(organizationId);

        return deals.stream().map(deal -> {
            var logoUrl = organizationRepository.findLogoUrlById(deal.getOrganizationId());
            return mapToDealsResponse(deal, logoUrl);
        }).toList();
    }

    private DealsResponse mapToDealsResponse(DealsEntity deal, String logoUrl) {

        var optionalOrganization = organizationRepository.findById(deal.getOrganizationId());
        if (optionalOrganization.isEmpty()) {
            return new DealsResponse(deal.getDealId(), null, null,
                    null, deal.getOfferDetail(),
                    deal.getStatus(), deal.getOrganizationId(), logoUrl);
        }
        var organization = optionalOrganization.get();
        return new DealsResponse(deal.getDealId(), organization.getName(), organization.getCompanyType(),
                organization.getBriefDescription(), deal.getOfferDetail(),
                deal.getStatus(), deal.getOrganizationId(), logoUrl);
    }

    public DealsJoinerEntity joinDeal(JoinDealRequest request) {
        var dealsJoiner = DealsJoinerEntity.builder()
                .dealId(request.dealId())
                .userId(request.userId())
                .organizationId(request.organizationId())
                .status(ApprovalRequestHistoryStatus.PENDING)
                .build();
        return dealsJoinerRepository.save(dealsJoiner);
    }

    public IntegrationDetails connectStripe(StripeConnectRequest request) {
        var integrationDetails = IntegrationDetails.builder()
                .organizationId(request.organizationId())
                .refreshToken(request.refreshToken())
                .integrationType(IntegrationType.STRIPE)
                .isConnected(true)
                .userId(request.userId())
                .connectedId(request.connectedId())
                .publishableKey(request.publishableKey())
                .build();
        return integrationRepository.save(integrationDetails);
    }

    public IntegrationDetails connectRazorpay(StripeConnectRequest request) {
        var integrationDetails = IntegrationDetails.builder()
                .organizationId(request.organizationId())
                .refreshToken(request.refreshToken())
                .integrationType(IntegrationType.RAZORPAY)
                .isConnected(true)
                .userId(request.userId())
                .connectedId(request.connectedId())
                .publishableKey(request.publishableKey())
                .build();
        return integrationRepository.save(integrationDetails);
    }

    public AddMoneyResponse addMoney(AddMoneyRequest request) {
        WalletEntity wallet = walletRepository.findByOrganizationId(request.organizationId());
        if (wallet == null) {
            wallet = new WalletEntity();
            wallet.setOrganizationId(request.organizationId());
            wallet.setAvailableAmount(0);
            wallet.setTotalAmount(0);
            walletRepository.save(wallet);
        }
        wallet.setAvailableAmount(wallet.getAvailableAmount() + request.amount());
        wallet.setTotalAmount(wallet.getTotalAmount() + request.amount());

        walletRepository.save(wallet);
        WalletTransactionEntity transaction = new WalletTransactionEntity();
        transaction.setWallet(wallet);
        transaction.setAmount(request.amount());
        transaction.setType("credit");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setOrderId(request.orderId());
        transaction.setPaymentId(request.paymentId());

        walletTransactionRepository.save(transaction);

        return new AddMoneyResponse(transaction.getId().toString(), transaction.getAmount(), transaction.getType(),
                wallet.getAvailableAmount(), wallet.getTotalAmount());
    }

    public List<DealJoinerResponse> getDealApplication(String dealId, ApprovalRequestHistoryStatus status) {
        var deals = dealsRepository.findByDealId(dealId);

        if (status != null) {
            return dealsJoinerRepository.findAllByDealIdAndStatus(dealId, status)
                    .stream().map(this::mapToDealJoinerResponse).toList();
        } else {
            return dealsJoinerRepository.findAllByDealId(dealId)
                    .stream().map(this::mapToDealJoinerResponse).toList();
        }
    }

    public DealJoinerResponse acceptDealApplication(Long applicationId) {
        var resp = dealsJoinerRepository.findById(applicationId);
        if (resp.isPresent()) {
            var deal = resp.get();
            String affiliateCode = RandomStringUtils.random(8, true, true);
            deal.setStatus(ApprovalRequestHistoryStatus.APPROVED);
            deal.setAffiliateCode(affiliateCode);
            var savedEntity = dealsJoinerRepository.save(deal);
            return mapToDealJoinerResponse(savedEntity);
        } else {
            return new DealJoinerResponse();
        }
    }

    public DealJoinerResponse rejectDealApplication(Long applicationId) {
        var resp = dealsJoinerRepository.findById(applicationId);
        if (resp.isPresent()) {
            var deal = resp.get();
            deal.setStatus(ApprovalRequestHistoryStatus.REJECTED);
            deal.setAffiliateCode(null);
            var savedEntity = dealsJoinerRepository.save(deal);
            return mapToDealJoinerResponse(savedEntity);
        } else {
            return new DealJoinerResponse();
        }
    }

    private DealJoinerResponse mapToDealJoinerResponse(DealsJoinerEntity dealsJoinerEntity) {
        var name = organizationRepository.findNameById(dealsJoinerEntity.getOrganizationId());
        var deals = dealsRepository.findByDealId(dealsJoinerEntity.getDealId());
        var dealJoinerResponse = DealJoinerResponse.builder()
                .id(dealsJoinerEntity.getId())
                .dealId(dealsJoinerEntity.getDealId())
                .userId(dealsJoinerEntity.getUserId())
                .organizationId(dealsJoinerEntity.getOrganizationId())
                .status(dealsJoinerEntity.getStatus())
                .organizationName(name);
        if (Objects.nonNull(dealsJoinerEntity.getAffiliateCode())) {
            var org = organizationRepository.findById(deals.getOrganizationId());
            if (org.isPresent()) {
                String referralLink = org.get().getWebsite() + "?affiliate_code=" + dealsJoinerEntity.getAffiliateCode();
                var prodUrl = String.format("%stracking/razorpay/%s", apiUrl, dealsJoinerEntity.getAffiliateCode());
                var testUrl = String.format("%stracking/razorpay/test/%s", apiUrl, dealsJoinerEntity.getAffiliateCode());
                dealJoinerResponse.affiliateCode(dealsJoinerEntity.getAffiliateCode())
                        .affiliateLink(referralLink)
                        .testWebhookUrl(testUrl)
                        .prodWebhookUrl(prodUrl);
            }
        }
        return dealJoinerResponse.build();
    }

    public PayoutMoneyResponse payout(PayoutMoneyRequest request) {
        try {
            razorpayClient = new RazorpayClient(razorPayKeyId, razorPayKeySecret);
            WalletEntity senderWallet = walletRepository.findByOrganizationId(request.organizationId());

            if (senderWallet == null) {
                throw new IllegalArgumentException("Sender wallet not found");
            }

            if (senderWallet.getAvailableAmount() < request.amount()) {
                throw new IllegalArgumentException("Insufficient balance for payout");
            }
            var receiverIntegration = integrationRepository.findByOrganizationIdAndIntegrationType(request.receiverOrganizationId(), IntegrationType.RAZORPAY);
            if (receiverIntegration == null || receiverIntegration.getConnectedId() == null) {
                throw new IllegalArgumentException("Receiver is not connected to Razorpay");
            }
            JSONObject transferRequest = new JSONObject();
            transferRequest.put("amount", (int) (request.amount() * 100));
            transferRequest.put("currency", "INR");
            transferRequest.put("account", receiverIntegration.getConnectedId());

            Transfer transfer = razorpayClient.transfers.create(transferRequest);
            senderWallet.setAvailableAmount(senderWallet.getAvailableAmount() - request.amount());
            senderWallet.setTotalAmount(senderWallet.getTotalAmount() - request.amount());


            // Store transaction details
            WalletTransactionEntity transaction = new WalletTransactionEntity();
            transaction.setWallet(senderWallet);
            transaction.setUserId(request.userId());
            transaction.setAmount(request.amount());
            transaction.setType("debit");
            transaction.setTimestamp(LocalDateTime.now());
            transaction.setOrderId(transfer.get("id"));
            transaction.setPaymentId(transfer.get("id"));
            senderWallet.getTransactions().add(transaction);
            walletRepository.save(senderWallet);
            walletTransactionRepository.save(transaction);

            RazorpayTrackingEntity payoutTracking = new RazorpayTrackingEntity();
            payoutTracking.setOrganizationId(request.organizationId());
            Organization senderOrg = organizationRepository.findById(request.organizationId()).orElse(null);
            if (senderOrg != null) {
                payoutTracking.setOrganizationId(Long.valueOf(senderOrg.getCode()));
            }

            payoutTracking.setAccountId(receiverIntegration.getConnectedId());
            payoutTracking.setEventType("payout");
            payoutTracking.setPaymentId(transfer.get("id"));
            payoutTracking.setOrderId(transfer.get("id"));
            payoutTracking.setAmount((int) (request.amount() * 100));
            payoutTracking.setCurrency("INR");
            payoutTracking.setStatus(transfer.get("status"));
            payoutTracking.setMethod("transfer");
            payoutTracking.setPayload(transfer.toString());

            razorpayTrackingRepository.save(payoutTracking);

            return new PayoutMoneyResponse(
                    transaction.getId().toString(),
                    transfer.get("id"),
                    request.amount(),
                    senderWallet.getAvailableAmount()
            );
        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.SH136, e.getMessage());
        }
    }

    public DealAffiliateResponse getAffiliateLink(Long organizationId, String dealId) {
        var dealJoiner = dealsJoinerRepository.findByOrganizationIdAndDealId(organizationId, dealId);
        if (Objects.nonNull(dealJoiner.getAffiliateCode())) {
//            var org = organizationRepository.findById(dealJoiner.getOrganizationId());
            var deals = dealsRepository.findByDealId(dealId);
            if (deals != null && deals.getPageURL() != null) {
                String referralLink = deals.getPageURL() + "?affiliate_code=" + dealJoiner.getAffiliateCode();
                var prodUrl = String.format("%stracking/razorpay/%s", apiUrl, dealJoiner.getAffiliateCode());
                var testUrl = String.format("%stracking/razorpay/test/%s", apiUrl, dealJoiner.getAffiliateCode());
                new DealAffiliateResponse(dealJoiner.getAffiliateCode(), referralLink, testUrl, prodUrl);
            }
        }
        return null;
    }

    public IntegrationDetails connectBank(BankAccountRequest request) {
        var integrationDetails = IntegrationDetails.builder()
                .organizationId(request.organizationId())
                .refreshToken(request.ifscCode())
                .integrationType(IntegrationType.BANK)
                .isConnected(true)
                .userId(request.userId())
                .connectedId(request.accountNumber())
                .publishableKey(request.holderName())
                .build();
        return integrationRepository.save(integrationDetails);
    }

    public ConnectedAccountsResponse checkConnectedAccounts(Long organizationId) {
        var razorPayConnected = integrationRepository.existsByOrganizationIdAndIntegrationTypeAndIsConnectedTrue(organizationId, IntegrationType.RAZORPAY);
        var stripeConnected = integrationRepository.existsByOrganizationIdAndIntegrationTypeAndIsConnectedTrue(organizationId, IntegrationType.STRIPE);
        var bankConnected = integrationRepository.existsByOrganizationIdAndIntegrationTypeAndIsConnectedTrue(organizationId, IntegrationType.BANK);
        return new ConnectedAccountsResponse(stripeConnected, bankConnected, razorPayConnected);
    }

    public DealDetailsResponse getDealDetails(String dealId) {
        var deal = dealsRepository.findByDealId(dealId);
        if (deal != null) {
            var dealDetailResponse = DealDetailsResponse.builder()
                    .dealId(dealId)
                    .offerDetail(deal.getOfferDetail())
                    .quotaRemaining(deal.getQuotaRemaining())
                    .approvalRequired(deal.isApprovalRequired())
                    .creationTimestamp(deal.getCreationTimestamp())
                    .geography(deal.getGeography())
                    .restrictedSectors(deal.getRestrictedSectors())
                    .channelAllowed(deal.getChannelAllowed())
                    .organizationId(deal.getOrganizationId())
                    .status(deal.getStatus());
            var organization = organizationRepository.findById(deal.getOrganizationId());
            organization.ifPresent(value -> {
                dealDetailResponse.organizationName(value.getName())
                        .organizationType(value.getCompanyType())
                        .organizationBrief(value.getBriefDescription())
                        .logoUrl(value.getLogoUrl());
            });
            return dealDetailResponse.build();
        } else {
            return null;
        }
    }

    public DealsEntity endDeal(String dealId) {
        DealsEntity deal = dealsRepository.findByDealId(dealId);
        if (deal == null) {
            throw new RuntimeException("Deal not found with id: " + dealId);
        }
        if (!"ACTIVE".equals(deal.getStatus())) {
            throw new RuntimeException("Deal must be active to end");
        }
        deal.setStatus("HIDDEN");
        return dealsRepository.save(deal);
    }

    public Map<String, String> getPayoutSummary(Long orgId) {
        Map<String, String> response = new HashMap<>();

        WalletEntity wallet = walletRepository.findByOrganizationId(orgId);
        if (wallet == null) {
            response.put("balance", "0");
            response.put("earning", "0");
            response.put("paid", "0");
            return response;
        }

        double totalEarnings = walletTransactionRepository.findByWalletAndType(wallet, "credit")
                .stream()
                .mapToDouble(WalletTransactionEntity::getAmount)
                .sum();

        double totalPaid = walletTransactionRepository.findByWalletAndType(wallet, "debit")
                .stream()
                .mapToDouble(WalletTransactionEntity::getAmount)
                .sum();

        response.put("balance", String.valueOf(wallet.getAvailableAmount()));
        response.put("earning", String.valueOf(totalEarnings));
        response.put("paid", String.valueOf(totalPaid));

        return response;
    }

    public CommissionRequestEntity createCommissionRequest(CommissionRequestRequest request) {
        String status = request.getOrgId().equals(request.getRequestingOrganizationId())
                ? "REQUEST_CREATED"
                : "REQUEST_RECEIVED";

        RazorpayTrackingEntity transaction = razorpayTrackingRepository.findByPaymentId(request.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + request.getTransactionId()));

        double transactionAmount = transaction.getAmount() / 100.0;
        double commissionAmount = transactionAmount * (request.getCommission() / 100.0);
        String name = organizationRepository.findNameById(request.getOrgId());

        return commissionRequestRepository.save(CommissionRequestEntity.builder()
                .organizationId(request.getOrgId())
                .requestingOrganizationId(request.getRequestingOrganizationId())
                .requestingOrganizationName(request.getRequestingOrganizationName())
                .status(status)
                .amount(commissionAmount)
                .date(LocalDate.now())
                .name(name)
                .invoiceAzure(request.getInvoiceAzure())
                .transactionId((request.getTransactionId()))
                .commissionPercentage(request.getCommission())
                .build());

    }

    public List<CommissionRequestResponse> getCommissionRequests(Long organizationId) {
        List<CommissionRequestEntity> entities = commissionRequestRepository
                .findByOrganizationId(organizationId);
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private CommissionRequestResponse convertToResponse(CommissionRequestEntity entity) {
        return CommissionRequestResponse.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .requestingOrganizationId(entity.getRequestingOrganizationId())
                .status(entity.getStatus())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .rejectingReason(entity.getRejectingReason())
                .name(entity.getName())
                .invoiceAzure(entity.getInvoiceAzure())
                .build();
    }

    public CommissionRequestEntity acceptCommissionRequest(Long id) {
        CommissionRequestEntity request = commissionRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission request not found"));
        request.setStatus(CommissionAction.ACCEPTED.name());
        return commissionRequestRepository.save(request);
    }

    public CommissionRequestEntity rejectCommissionRequest(Long id, String rejectingReason) {
        CommissionRequestEntity request = commissionRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission request not found"));
        request.setStatus(CommissionAction.REJECTED.name());
        request.setRejectingReason(rejectingReason);
        return commissionRequestRepository.save(request);
    }

    public List<CapturedPaymentResponse> getCapturedPayments(Long organizationId) {
        List<RazorpayTrackingEntity> payments = razorpayTrackingRepository.findByOrganizationIdAndStatus(organizationId, "captured");
        return payments.stream()
                .map(this::mapToCapturedPaymentResponse)
                .collect(Collectors.toList());
    }

    private CapturedPaymentResponse mapToCapturedPaymentResponse(RazorpayTrackingEntity entity) {

        String partnerName = null;
        String partnerId = null;
        if (entity.getAffiliateCode() != null) {
            Optional<Organization> partnerOrg = organizationRepository.findByCode(entity.getAffiliateCode());
            partnerName = partnerOrg.map(Organization::getName).orElse(null);
            partnerId = partnerOrg.map(org -> String.valueOf(org.getId())).orElse(null);
        }

        return CapturedPaymentResponse.builder()
                .paymentId(entity.getPaymentId())
                .amount(entity.getAmount() / 100.0)
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .method(entity.getMethod())
                .bank(entity.getBank())
                .contact(entity.getContact())
                .email(entity.getEmail())
                .timestamp(entity.getCreationTimestamp())
                .partnerName(partnerName)
                .partnerId(partnerId)
                .build();
    }
}

