package hudson.plugins.jira;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;

/**
 * Parses build changelog for JIRA issue IDs and then
 * updates JIRA issues accordingly. Workflow compat class.
 *
 * @author Kohsuke Kawaguchi
 */
public class JiraIssueWorkflowUpdater extends Recorder implements SimpleBuildStep {
    public static String status;
    private UpdaterIssueSelector issueSelector;

    @DataBoundConstructor
    public JiraIssueWorkflowUpdater(String status) {
        this.status = status;
    }

    @Override
    public void perform(Run<?, ?> build, FilePath filePath, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        Updater.perform(build, listener, getIssueSelector());
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    public UpdaterIssueSelector getIssueSelector() {
        UpdaterIssueSelector uis = this.issueSelector;
        if (uis == null) uis = new DefaultUpdaterIssueSelector();
        return (this.issueSelector = uis);
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public String getDisplayName() {
            return "Jira ticket updater";
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
    }
}
