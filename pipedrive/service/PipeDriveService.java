package com.sharkdom.pipedrive.service;

import com.sharkdom.pipedrive.dto.CreatePersonRequest;
import com.sharkdom.pipedrive.dto.CreatePersonResponse;
import com.sharkdom.pipedrive.dto.PersonFieldsResponse;
import com.sharkdom.pipedrive.dto.PersonsResponse;

public interface PipeDriveService {

    public PersonFieldsResponse getContacts();

    public PersonFieldsResponse getContactsByUserId(String userId);

    public PersonsResponse getDetails();

    public PersonsResponse getDetailsByUserId(String userId);

    public CreatePersonResponse createPerson(CreatePersonRequest createPersonRequest);

    public CreatePersonResponse createPersonByUser(CreatePersonRequest createPersonRequest);

}
