package org.chamsoft.smarthome.userpermissions.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Email {

    @Column(name = "email_address")
    private String value;

}
