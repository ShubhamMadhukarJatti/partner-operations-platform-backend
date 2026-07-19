package com.sharkdom.entity.user;

import com.sharkdom.constants.user.ConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_other_connections")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class UserOtherConnections extends UserConnections {

    private static final long serialVersionUID = 1L;
    ConnectionStatus status;

    public UserOtherConnections(String userId, String connectionUserId, ConnectionStatus status) {
        super(userId, connectionUserId);
        this.status = status;
    }

}
