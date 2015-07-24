package org.jenkinsci.plugins.phpwebserver;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.Map;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class PhpWebserverBuildWrapper extends BuildWrapper {

	private static final PhpWebserverConnector connector = new PhpWebserverConnector();
	
	// TODO expose other options ?
	// TODO make sure PHP 5.4
	// TODO allow to set path to php binary ?
	private final int port;
	private final String root;

	@DataBoundConstructor
	public PhpWebserverBuildWrapper(int port, String root) {
		this.port = port;
		this.root = root;
	}

	public int getPort() {
		return port;
	}
	
	public String getRoot() {
		return root;
	}
	
	@Override
	public Environment setUp(AbstractBuild build, final Launcher launcher, BuildListener listener) throws IOException, InterruptedException {		
		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				connector.stop(port, root, build.getId());
				return true;
			}
		};
	}
	
	@Override
	public void makeBuildVariables(AbstractBuild build, Map<String,String> variables) {
		String url = connector.start(port, root, build.getId());
		variables.put(new String("selenium"), url);
    }

    @Override
    public Environment setUp(Build build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        return setUp(build, launcher, listener);
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        public DescriptorImpl() {
            super(PhpWebserverBuildWrapper.class);
        }
        
        public FormValidation doCheckPort(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            if (0 == value.length()) {
            	return FormValidation.error("Please set a port number");
            }
            try {
            	int port = Integer.parseInt(value);
            	if (port < 0 || port > 65535) {
            		return FormValidation.error("Wrong port. Should be 0 <= port <= 65535");
            	}
            }
            catch (NumberFormatException e) {
            	return FormValidation.error("Wrong port. Should be a numerical value");
			}
        	return FormValidation.ok();
        }

        public FormValidation doCheckRoot(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            if (0 == value.length()) {
            	return FormValidation.error("Please set a root directory");
            }
        	return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return "Run a PHP built-in web server";
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }
}
