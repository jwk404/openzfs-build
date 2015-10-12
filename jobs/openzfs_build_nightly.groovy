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
         * This parameter allows a user of this job to provide a
         * specific branch or git hash that should be checked out,
         * built, and tested. This is particularly useful for building
         * and testing pull GitHub request commits prior to integrating
         * them on the "master" branch.
         */
        stringParam('GIT_BRANCH', 'master',
            'The name of the git branch or the git hash to checkout')
    }

    scm {
        git {
            /*
             * In order to support building pull requests, we need to
             * tweak the refspec to ensure we fetch pull request commits
             * as well. By default, none of the pull requests would be
             * fetched, which would cause failures if a pull request
             * commit was passed in as the "GIT_BRANCH" parameter of
             * this job.
             */
            remote {
                github('illumos/illumos-gate')
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }
            branch('${GIT_BRANCH}')
        }
    }

    steps {
        /*
         * The "build-os.sh" script will be in "/usr/local/build-os" on
         * the build slave. Additionally, it requires the CI_SH_LIB
         * environment variable to be set, which it uses to find and
         * source a couple dependent bash "libraries" that it uses. This
         * variable should be set to the directory that contains the
         * "build-os.sh" script along with the dependent libraries.
         */
        shell('CI_SH_LIB=/usr/local/build-os /usr/local/build-os/build-os.sh')
    }
}