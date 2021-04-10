package blog.technodeck.auth.entity;

import java.time.ZonedDateTime;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class AccessToken extends PanacheEntity {

    public Long userId;
    public String token;
    public ZonedDateTime expires;
    
}
