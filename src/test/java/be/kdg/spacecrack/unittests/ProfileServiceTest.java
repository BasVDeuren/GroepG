package be.kdg.spacecrack.unittests;

import be.kdg.spacecrack.Exceptions.SpaceCrackAlreadyExistsException;
import be.kdg.spacecrack.controllers.TokenController;
import be.kdg.spacecrack.model.AccessToken;
import be.kdg.spacecrack.model.Profile;
import be.kdg.spacecrack.model.User;
import be.kdg.spacecrack.repositories.ProfileRepository;
import be.kdg.spacecrack.repositories.TokenRepository;
import be.kdg.spacecrack.repositories.UserRepository;
import be.kdg.spacecrack.services.AuthorizationService;
import be.kdg.spacecrack.services.ProfileService;
import be.kdg.spacecrack.utilities.TokenStringGenerator;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.transaction.annotation.Transactional;

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
    UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        TokenStringGenerator generator = new TokenStringGenerator();
        TokenRepository tokenRepository = new TokenRepository(sessionFactory);
        userRepository = mock(UserRepository.class);
        tokenController = new TokenController(new AuthorizationService(tokenRepository, userRepository, generator ));
    }

    @Test
    @Transactional
    public void testCreateContact() throws Exception {
        Session session;

        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProfileService contactService = new ProfileService(profileRepository, userRepository);

        User user = new User("username", "password", "email",true);
        session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(user);

        stub(userRepository.getUser(user)).toReturn(user);
        AccessToken accessToken = tokenController.login(user);

        Calendar calendar = new GregorianCalendar(2013,1,5);

        Profile profile = new Profile("firstname","lastname", calendar.getTime(),"image");
        contactService.createProfile(profile, user);
        verify(profileRepository, VerificationModeFactory.times(1)).createProfile(profile);
    }

    @Test(expected = SpaceCrackAlreadyExistsException.class)
    @Transactional
    public void testCreateExtraContact_notPossible() throws Exception {
        Session session;

        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProfileService profileService = new ProfileService(profileRepository, userRepository);

        User user = new User("username", "password", "email", true);
        session = sessionFactory.getCurrentSession();

        session.saveOrUpdate(user);

        stub(userRepository.getUser(user)).toReturn(user);
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
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ProfileService profileService = new ProfileService(profileRepository, userRepository);
        tokenController = mock(TokenController.class);

        User user = new User("username", "password", "email", true);

        AccessToken token = new AccessToken("accesstoken123");
        stub(tokenController.login(user)).toReturn(token);

        AccessToken accessToken = tokenController.login(user);

        Calendar calendar = new GregorianCalendar(2013,2,12);
        Profile profile = new Profile("firstname","lastname", calendar.getTime() ,"image");
        profileService.createProfile(profile, user);
        profile.setFirstname("newFirstname");
        stub(userRepository.getUserByAccessToken(token)).toReturn(user);
        profileService.editProfile(profile);

        verify(profileRepository, VerificationModeFactory.times(1)).editContact(profile);
    }
}
