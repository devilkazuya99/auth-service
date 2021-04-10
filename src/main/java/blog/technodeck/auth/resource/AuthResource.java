package blog.technodeck.auth.resource;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blog.technodeck.auth.entity.AccessToken;
import blog.technodeck.auth.entity.User;
import io.quarkus.panache.common.Sort;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    Logger logger = LoggerFactory.getLogger(getClass());
    
    @Transactional // we are saving data, so turn on transaction
    @POST
    @Path("/register")
    public User register(User user) {
        logger.info("Registering user: {}", user.username);
        user.persist();
        return user;
    }
 
    @Transactional
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public AccessToken login(
            @FormParam("username") String username, 
            @FormParam("password") String password) {
        logger.info("{} : {}", username, password);
        try {
            User user = User.find("username = ?1 and password = ?2", username, password)
                    .singleResult();
            AccessToken accessToken = new AccessToken();
            accessToken.userId = user.id;
            accessToken.token = UUID.randomUUID().toString();
            accessToken.expires = ZonedDateTime.now().plusMinutes(30);
            accessToken.persist();
            return accessToken;
        } catch(Exception e) {
            Response response = Response.status(Response.Status.NOT_FOUND)
                                        .entity(e.getMessage())
                                        .build();
            throw new NotFoundException(response);
        }
    }
    
    @GET
    @Path("/verify/{id}/{token}")
    public void verify(@PathParam("id") Long id, @PathParam("token") String token) {
        AccessToken accessToken = 
                        AccessToken.find(
                            "userId = ?1 and token = ?2", Sort.by("id").descending(), id, token
                        ).firstResult();
        if(accessToken == null) {
            logger.info("Invalid token.");
            throw new ForbiddenException("Invalid token");
        }
        if(accessToken.expires.isBefore(ZonedDateTime.now())) {
            logger.info("Token expired.");
            throw new ForbiddenException("Token Expired");
        }
    }
    
    @GET
    @Path("/list")
    public List<User> list() {
        return User.findAll().list();
    }
    
}
