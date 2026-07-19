package com.sharkdom.service.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.user.User;
import com.sharkdom.model.email.EmailReqModelWithMultipartAttachments;
import com.sharkdom.model.email.EmailTemplate;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.organization.OrganizationService;
import com.sharkdom.service.user.UserService;
import com.sharkdom.util.firestore.service.FirestoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class EmailFromTemplateService {
    private final UserService userService;
    private final OrganizationService organizationService;
    private final ObjectMapper oMapper;
    private final UserRepository userRepository;

    public EmailFromTemplateService(UserService userService, OrganizationService organizationService, ObjectMapper oMapper, UserRepository userRepository) {
        this.userService = userService;
        this.organizationService = organizationService;
        this.oMapper = oMapper;
        this.userRepository = userRepository;
    }

    public EmailTemplate getTemplateByCode(String code) throws InterruptedException, ExecutionException {
        DocumentReference docRef = FirestoreService.getDb().collection("EmailTemplates").document(code);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(EmailTemplate.class);
        } else {
            log.error("Cannot fetch email template " + code + " from firebase!");
            throw new RuntimeException("Cannot find EmailTemplate with code " + code);

        }
    }

    public List<String> getAllJavaTags(String template) {
        Pattern TAG_REGEX = Pattern.compile("<java>(.+?)</java>", Pattern.DOTALL);
        final List<String> tagValues = new ArrayList<String>();
        final Matcher matcher = TAG_REGEX.matcher(template);
        while (matcher.find()) {
            tagValues.add(matcher.group(1));
        }
        return tagValues;
    }

    List<Map<String, Object>> getUserDataForTemplate(List<String> userIdList, Map<String, Map<String, Object>> additionalData) {

        List<User> users = userService.findAllByUserIdIn(userIdList);
        ObjectMapper oMapper = new ObjectMapper();
        List<Map<String, Object>> userMaps = new ArrayList<>();

        users.stream().forEach(user -> {
            Map<String, Object> dataMap = oMapper.convertValue(user, Map.class);
            Map<String, Object> appendedUserDataMap = new HashMap<String, Object>();
            // append user. at the beginning of each key and the add resultant map into
            // userMaps
            dataMap.forEach((k, v) -> {
                if (k.equals("additionalDetails")) {
                    List<LinkedHashMap<Object, Object>> additionalDetails = (List<LinkedHashMap<Object, Object>>) v;
                    additionalDetails.forEach(dtl -> appendedUserDataMap
                            .put("user.additionalDetails." + dtl.get("dataKey"), dtl.get("dataValue")));
                } else {
                    appendedUserDataMap.put("user." + k, v);
                }
            });

            //add additionalDetails
            if (null != additionalData && null != additionalData.get(user.getUserId())) {
                appendedUserDataMap.putAll(additionalData.get(user.getUserId()));
            }
            userMaps.add(appendedUserDataMap);
        });
        return userMaps;
    }

    List<Map<String, Object>> getOrganizationDataForTemplate(List<Long> organizationIdList, Map<String, Map<String, Object>> additionalData) {

        List<Organization> organizations = organizationService.findAllByOrganizationIdIn(organizationIdList);

        List<Map<String, Object>> organizationMaps = new ArrayList<>();
        organizations.forEach(organization -> {
            Map<String, Object> dataMap = oMapper.convertValue(organization, Map.class);
            Map<String, Object> appendedOrganizationDataMap = new HashMap<>();

            dataMap.forEach((k, v) -> {
                appendedOrganizationDataMap.put("organization." + k, v);
            });

            //add additionalDetails
            if (null != additionalData && null != additionalData.get(organization.getId().toString())) {
                appendedOrganizationDataMap.putAll(additionalData.get(organization.getId().toString()));
            }
            organizationMaps.add(appendedOrganizationDataMap);
        });
        return organizationMaps;
    }

    public List<EmailReqModelWithMultipartAttachments> prepareEmailsForUsers(EmailTemplate template, List<String> userIdList, Map<String, Map<String, Object>> additionalData) {

        List<Map<String, Object>> userMaps = getUserDataForTemplate(userIdList, additionalData);

        List<EmailReqModelWithMultipartAttachments> emailReqModelWithMultipartAttachments = new ArrayList<>();
        // iterate for each user
        for (Map<String, Object> userMap : userMaps) {
            emailReqModelWithMultipartAttachments.add(
                    prepareEmail(template, Collections.singletonList(String.valueOf(userMap.get("user.email"))), userMap));
        }
        return emailReqModelWithMultipartAttachments;
    }

    public EmailReqModelWithMultipartAttachments prepareEmail(EmailTemplate template, List<String> recipients, Map<String, Object> dataMap) {
        String populatedTemplate = populateTemplateWithValues(template, dataMap);
        return new EmailReqModelWithMultipartAttachments(template.getSender(),
                recipients, populatedTemplate, populatedTemplate,
                template.getSubject(), template.getS3AttachmentNames() == null ? Arrays.asList() : Arrays.asList(template.getS3AttachmentNames().split(",")), null, template.getSender());
    }

    public List<EmailReqModelWithMultipartAttachments> prepareEmailsForOrganizations(EmailTemplate template, List<Long> organizationIdList, Map<String, Map<String, Object>> additionalData) {

        List<Map<String, Object>> organizationMaps = getOrganizationDataForTemplate(organizationIdList, additionalData);

        List<EmailReqModelWithMultipartAttachments> emailReqModelWithMultipartAttachments = new ArrayList<>();
        // iterate for each organization
        organizationMaps.forEach(organizationMap -> {
            String primaryEmail = (String) organizationMap.get("organization.primaryEmail");
            var user = userRepository.findByEmail(primaryEmail);
            var organizationName = (String) organizationMap.get("organization.name");
            if (user.isPresent()) {
                organizationName = user.get().getName();
            }
            String receiverField = String.format("%s <%s>", organizationName, primaryEmail);
            emailReqModelWithMultipartAttachments.add(
                    prepareEmail(template, Collections.singletonList(receiverField), organizationMap));
        });
        return emailReqModelWithMultipartAttachments;
    }

    private String populateTemplateWithValues(EmailTemplate template, Map<String, Object> dataMap) {

        Map<String, String> tagsWithResults = new HashMap<>();
        List<String> allEvaluationTags = getAllJavaTags(template.getBodyHtml());
        // iterate for each tag
        for (final String tagWithDiv : allEvaluationTags) {
            String htmlPart = tagWithDiv;
            if (tagWithDiv.contains("<<if(")) {
                String tag = tagWithDiv.substring(0, tagWithDiv.indexOf(">>") + 2);
                // if condition is true store actual html present inside the tag else store ""
                if (tagConditionIsTrue(tag, dataMap)) {
                    htmlPart = tagWithDiv.substring(tagWithDiv.indexOf(">>") + 2);
                } else {
                    htmlPart = "";
                }
            }
            String htmlPartAfterAddingValues = replacePlaceHoldersWithActualValues(dataMap, htmlPart);
            tagsWithResults.put(tagWithDiv, htmlPartAfterAddingValues);
        }

        String tempalateWithValues = template.getBodyHtml();
        for (Map.Entry<String, String> entry : tagsWithResults.entrySet()) {
            tempalateWithValues = tempalateWithValues.replace(entry.getKey(), entry.getValue());
        }
        return tempalateWithValues.replaceAll("<java>", "").replaceAll("</java>", "");
    }

    // replaces placeholder with actual value
    // {{user.name}} will be replaced with satish
    private String replacePlaceHoldersWithActualValues(Map<String, Object> userMap, String htmlPart) {
        if (!htmlPart.contains("{{")) {
            return htmlPart;
        }
        String placeHolder = htmlPart.substring(htmlPart.indexOf("{{"), htmlPart.indexOf("}}") + 2);
        String key = placeHolder.replace("{{", "").replace("}}", "");
        String value = String.valueOf(userMap.get(key));
        String htmlAfterAddingValues = htmlPart.replace(placeHolder, value);
        return htmlAfterAddingValues;
    }

    // a tag looks like: <<if(user.can_collaborate==false)>> "<div> Data that you
    // want to show conditionally </div>"
    // so if condition is true then return "<div> Data that you want to show
    // conditionally </div>"
    // if condition is false then return ""
    private boolean tagConditionIsTrue(String tag, Map<String, Object> userMap) {
        tag = removeAllWhiteSpaces(tag);

        if (!tag.contains("<<if(")) {
            return true;
        }
        String fieldName = getFieldNameFromEvalTag(tag);
        String desiredValue = getFieldValueFromEvalTag(tag);
        return desiredValue.equals(String.valueOf(userMap.get(fieldName)));
    }

    private String getFieldNameFromEvalTag(String tag) {
        int fieldNameStartIndex = tag.indexOf("(") + 1;
        int fieldNameEndIndex = tag.indexOf("==");
        return tag.substring(fieldNameStartIndex, fieldNameEndIndex);
    }

    private String getFieldValueFromEvalTag(String tag) {
        int fieldValueStartIndex = tag.indexOf("==") + 2;
        int fieldValueEndIndex = tag.indexOf(")");
        return tag.substring(fieldValueStartIndex, fieldValueEndIndex);
    }

    private String removeAllWhiteSpaces(String text) {
        return text.replaceAll("\\s", "");
    }

}
