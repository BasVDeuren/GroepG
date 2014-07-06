package be.kdg.spacecrack.integrationtests;
/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.viewmodels.GameActivePlayerWrapper;
import be.kdg.spacecrack.viewmodels.GameViewModel;
import be.kdg.spacecrack.viewmodels.RevisionListViewModel;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IntegrationReplayControllerTests extends BaseFilteredIntegrationTests {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void getRevisionByNumber_validGameId_Revision() throws Exception {
        PlatformTransactionManager transactionManager = (PlatformTransactionManager) applicationContext.getBean("transactionManager");
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        String accessToken = loginAndRetrieveAccessToken();
        GameActivePlayerWrapper gameActivePlayerWrapper = createAGame(accessToken);
        GameViewModel game = gameActivePlayerWrapper.getGame();

        transactionManager.commit(status);
        TransactionStatus status1 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        MvcResult result = mockMvc.perform(get("/auth/replay/" + game.getGameId())
                .accept(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisions[0]", CoreMatchers.not(0)))
                .andExpect(jsonPath("$.revisions", Matchers.hasSize(1)))
                .andReturn();

        transactionManager.commit(status1);
        TransactionStatus status2 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        String contentAsString = result.getResponse().getContentAsString();
        RevisionListViewModel revisionListViewModel = objectMapper.readValue(contentAsString, RevisionListViewModel.class);
        String urlTemplate = "/auth/replay/" + game.getGameId() + "/" + revisionListViewModel.getRevisions().get(0);
        mockMvc.perform(get(urlTemplate)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId", CoreMatchers.is(game.getGameId())))
                .andExpect(jsonPath("$.gameId", CoreMatchers.is(game.getGameId())));

        transactionManager.commit(status2);
    }
}
