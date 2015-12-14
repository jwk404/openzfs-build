/*
 * Perform a full nightly build of OpenZFS on a build slave.
 */
job("openzfs-build-nightly") {
    /*
     * This job can only be run on build slaves that are properly
     * configured to support OpenZFS builds. This label is attached
     * to Jenkins slaves that meet this criteria, and this line will
     * ensure we only schedule this job on those systems.
     */
    label 'openzfs-build-slave'

    /*
     * We must explicitly enable concurrent builds, otherwise the
     * default option is to only allow a single build at a time.
     */
    concurrentBuild()

    /*
     * The "build-os.sh" script will emit output that includes ANSI
     * color escape sequences. In order to view these properly in the
     * Jenkins job's console page, we need to explicitly enable this
     * option.
     */
    wrappers {
        colorizeOutput()
    }

    parameters {
        /*
         * This parameter allows a consumer of this job a way to specify
         * which git commit or branch should be built and tested. This
         * is particularly useful for building and testing GitHub pull
         * requests via the GitHub Pull Request Builder plugin. By
         * default, the "master" branch will be used.
         */
        stringParam('sha1', 'origin/master',
            'The git commit hash or branch name to build and test.')

        nodeParam('NODE') {
            description('The node used to run this job.')
        }
    }

    scm {
        git {
            /*
             * In order to support building pull requests, we need to
             * tweak the refspec to ensure we fetch pull request commits
             * as well. By default, none of the pull requests would be
             * fetched, which would cause failures if a pull request
             * commit was passed in as the "sha1" parameter of this job.
             */
            remote {
                github('openzfs/openzfs')
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }
            branch('${sha1}')
        }
    }

    steps {
        /*
         * The "build-os.sh" and "install-os.sh" scripts will be found
         * in "/usr/local" on the build slave. Additionally they require
         * the CI_SH_LIB environment variable to be set, which they use
         * to find and source a dependent bash "library" that they each
         * use. This variable should also be set to "/usr/local", since
         * the dependent library will also be installed there.
         */
        shell('CI_SH_LIB=/usr/local /usr/local/build-os.sh')
        shell('sudo CI_SH_LIB=/usr/local su root -c /usr/local/install-os.sh')
    }
}
