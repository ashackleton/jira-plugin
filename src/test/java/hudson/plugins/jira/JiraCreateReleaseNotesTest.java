package hudson.plugins.jira;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import hudson.model.Job;
import hudson.model.Run;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.PrintWriter;
import jenkins.tasks.SimpleBuildWrapper;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class JiraCreateReleaseNotesTest {

    private static final String JIRA_RELEASE = Long.toString(System.currentTimeMillis());
    private static final String JIRA_PRJ = "TEST_PRJ";
    private static final String JIRA_RELEASE_PARAM = "${JIRA_RELEASE}";
    private static final String JIRA_PRJ_PARAM = "${JIRA_PRJ}";
    private static final String JIRA_VARIABLE = "ReleaseNotes";
    private static final String JIRA_OTHER_FILTER = "status in (Resolved, Done, Closed)";

    @Mock
    Run build;
    @Mock
    Launcher launcher;
    @Mock
    TaskListener taskListener;
    @Mock
    EnvVars env;
    @Mock
    Job project;
    @Mock
    JiraSite site;
    @Mock
    private PrintWriter printWriter;

    @Before
    public void createCommonMocks() throws IOException, InterruptedException {
        when(build.getParent()).thenReturn(project);
        when(build.getEnvironment(taskListener)).thenReturn(env);
        when(taskListener.fatalError(Mockito.anyString(), Mockito.anyVararg())).thenReturn(printWriter);
        when(build.getResult()).thenCallRealMethod();

        when(env.expand(Mockito.anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                String expanded = (String) args[0];
                if (expanded.equals(JIRA_PRJ_PARAM))
                    return JIRA_PRJ;
                else if (expanded.equals(JIRA_RELEASE_PARAM))
                    return JIRA_RELEASE;
                else
                    return expanded;
            }
        });

    }

    @Test
    public void testDefaults(){
        JiraCreateReleaseNotes jcrn = new JiraCreateReleaseNotes(JIRA_PRJ,JIRA_RELEASE,"");
        assertEquals(JiraCreateReleaseNotes.DEFAULT_ENVVAR_NAME, jcrn.getJiraEnvironmentVariable());
        assertEquals(JiraCreateReleaseNotes.DEFAULT_FILTER, jcrn.getJiraFilter());
    }

    @Test
    public void jiraApiCallDefaultFilter() throws InterruptedException, IOException {
        JiraCreateReleaseNotes jcrn = spy(new JiraCreateReleaseNotes(JIRA_PRJ,JIRA_RELEASE,JIRA_VARIABLE));
        doReturn(site).when(jcrn).getSiteForProject((Job<?, ?>) Mockito.any());
        SimpleBuildWrapper.Context c = new SimpleBuildWrapper.Context();
        jcrn.setUp(c, build, null, launcher, taskListener, null);
        verify(site).getReleaseNotesForFixVersion(JIRA_PRJ, JIRA_RELEASE, JiraCreateReleaseNotes.DEFAULT_FILTER);
    }

    @Test
    public void jiraApiCallOtherFilter() throws InterruptedException, IOException {
        JiraCreateReleaseNotes jcrn = spy(new JiraCreateReleaseNotes(JIRA_PRJ,JIRA_RELEASE,JIRA_VARIABLE, JIRA_OTHER_FILTER));
        doReturn(site).when(jcrn).getSiteForProject((AbstractProject<?, ?>) Mockito.any());
        SimpleBuildWrapper.Context c = new SimpleBuildWrapper.Context();
        jcrn.setUp(c, build, null, launcher, taskListener, null);
        verify(site).getReleaseNotesForFixVersion(JIRA_PRJ, JIRA_RELEASE, JIRA_OTHER_FILTER);
    }

    @Test
    public void failBuildOnError() throws InterruptedException, IOException {
        JiraCreateReleaseNotes jcrn = spy(new JiraCreateReleaseNotes("",JIRA_RELEASE,JIRA_VARIABLE, JIRA_OTHER_FILTER));
        doReturn(site).when(jcrn).getSiteForProject((AbstractProject<?, ?>) Mockito.any());
        SimpleBuildWrapper.Context c = new SimpleBuildWrapper.Context();
        jcrn.setUp(c, build, null, launcher, taskListener, null);
    }
}
