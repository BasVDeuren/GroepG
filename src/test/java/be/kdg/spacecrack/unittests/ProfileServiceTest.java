package be.kdg.spacecrack.unittests;

import be.kdg.spacecrack.Exceptions.SpaceCrackAlreadyExistsException;
import be.kdg.spacecrack.controllers.TokenController;
import be.kdg.spacecrack.model.AccessToken;
import be.kdg.spacecrack.model.Profile;
import be.kdg.spacecrack.model.User;
import be.kdg.spacecrack.repositories.IProfileRepository;
import be.kdg.spacecrack.repositories.ITokenRepository;
import be.kdg.spacecrack.repositories.IUserRepository;
import be.kdg.spacecrack.services.AuthorizationService;
import be.kdg.spacecrack.services.ProfileService;
import be.kdg.spacecrack.utilities.TokenStringGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.mockito.Mockito.*;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
public class ProfileServiceTest extends BaseUnitTest{
    private TokenController tokenController;

    @Autowired
    IUserRepository userRepository;
    @Autowired
    ITokenRepository tokenRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Before
    public void setUp() throws Exception {
        TokenStringGenerator generator = new TokenStringGenerator();

        userRepository = mock(IUserRepository.class);
        tokenController = new TokenController(new AuthorizationService(tokenRepository, userRepository, generator ));
    }

    @Test
    @Transactional
    public void testCreateContact() throws Exception {
        EntityManager entityManager;

        IProfileRepository profileRepository = mock(IProfileRepository.class);
        ProfileService contactService = new ProfileService(profileRepository, userRepository);

        User user = new User("username", "password", "email",true);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.persist(user);

        stub(userRepository.getUser("email", "password")).toReturn(user);
        AccessToken accessToken = tokenController.login(user);

        Calendar calendar = new GregorianCalendar(2013,1,5);

        Profile profile = new Profile("firstname","lastname", calendar.getTime(),"image");
        contactService.createProfile(profile, user);
        verify(profileRepository, VerificationModeFactory.times(1)).save(profile);
    }

    @Test(expected = SpaceCrackAlreadyExistsException.class)
    @Transactional
    public void testCreateExtraContact_notPossible() throws Exception {
        EntityManager entityManager;

        IProfileRepository profileRepository = mock(IProfileRepository.class);
        ProfileService profileService = new ProfileService(profileRepository, userRepository);

        User user = new User("username", "password", "email", true);
        entityManager = entityManagerFactory.createEntityManager();

        entityManager.persist(user);

        stub(userRepository.getUser("email", "password" )).toReturn(user);
        AccessToken accessToken = tokenController.login(user);
        Calendar calendar = new GregorianCalendar(2013,1,5);

        Profile profile = new Profile("firstname","lastname", calendar.getTime(),"image");
        Profile profile2 = new Profile("firstname","lastname", calendar.getTime(),"image");
        profileService.createProfile(profile, user);
        profileService.createProfile(profile2, user);
    }

    @Test
    @Transactional
    public void testEditProfile_validProfile() throws Exception {
        IProfileRepository profileRepository = mock(IProfileRepository.class);
        ProfileService profileService = new ProfileService(profileRepository, userRepository);
        tokenController = mock(TokenController.class);

        User user = new User("username", "password", "email", true);

        AccessToken token = new AccessToken("accesstoken123");
        stub(tokenController.login(user)).toReturn(token);

        tokenController.login(user);

        Calendar calendar = new GregorianCalendar(2013,2,12);
        Profile profile = new Profile("firstname","lastname", calendar.getTime() ,"image");
        profileService.createProfile(profile, user);
        profile.setFirstname("newFirstname");
        stub(userRepository.getUserByAccessToken(token)).toReturn(user);
        profileService.editProfile(profile);

        verify(profileRepository, VerificationModeFactory.times(2)).save(profile);
    }
}
