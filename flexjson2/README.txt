== Introduction ==

This page will guide the development team with setting up their environment to perform a release.


== Prerequisites  ==

 * JDK 5+ is installed on your command line path.
 * Install Maven 2.2.1 or higher.  2.2.0 has a bug that will produce invalid checksums.
 * Subversion 1.5+ is installed on your command line path. For more information, please refer to
   http://subversion.apache.org/.
 * Install/Configure GPG - The artifacts that are deployed to the central maven repositories need
   to be signed.  To do this you will need to have a public and private keypair.  There is a very
   good guide [http://www.sonatype.com/people/2010/01/how-to-generate-pgp-signatures-with-maven/]
   that will walk you through this.

== Configuration ==

=== Maven ===

As of Maven 2.1.0 you can now encrypt your servers passwords.  We highly recommend that you follow
this guide [http://maven.apache.org/guides/mini/guide-encryption.html] to set your master password
and use it to encrypt your Sonatype password in the next section.


=== Sonatype ===

Using the instructions from the previous step encrypt your Sonatype password and add the following
servers to your ~/.m2/settings.xml file.  You may already have other servers in this file.  If not
just create the file. You will need to change the username and password to your own.

<?xml version="1.0" encoding="UTF-8"?>
<settings>
  <servers>
    <server>
      <id>sonatype-nexus-snapshots</id>
      <username>bgoodin</username>
      <password>{jXMOWnoPFgsHLpMvz5VrIt9kRbzGpI8u+9EF1iFQyJQ=}</password>
    </server>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>bgoodin</username>
      <password>{jXMOWnoPFgsHLpMvz5VrIt9kRbzGpI8u+9EF1iFQyJQ=}</password>
    </server>
  </servers>
</settings>



== Release ==

The release plugin for maven is already configured in the flexjson pom file so all you need to do
is execute the following two steps to complete the release.  The first step will create the release
tag and update the pom with the correct release and snapshot versions.  The second step will sign
and deploy the artifacts to the Sonatype open source repository.  This repository is synced every
hour to the central Maven repositories.  If you don't supply the optional gpg.passphrase then you
will be prompted for it.

 # (Optional) Run a Prepare "dry run"
   mvn -DdryRun=true release:prepare

   mvn release:clean

 # Prepare the release
   mvn -Dusername=[your_scm_username] -Dpassword=[your_scm_password] release:prepare

 # Perform
   mvn release:perform -Dgpg.passphrase=thephrase[or enter it interactively]

== OSS Sonatype Staging ==

 # Close staging

 Once the project has been built and uploaded with maven you'll need to login to https://oss.sonatype.org.

 Click "Staging Repositories" on the left.

 You will see flexjson in the the Repository list. Highlight it and visually verify that everything looks
 correct by expanding the treeview that pops up at the bottom of the screen.

 Once you are confident click the checkbox next to the flexjson repository and click the "Close" button
 located above the repository list.

 Fill in a comment like "closing staging" and click okay. This will close the staging phase and you will
 then move on to the release phase.

 # Release

 In order to finally release flexjson you simply need to click the checkbox next to flexjson and then clck
 the "Release" button located above the repository list.

 Voila! FlexJSON will be released to the central maven repository after a short while.

== Build Downloadable Distributions ==

After completing the release to the maven repository it is time to build the downloadable
distributions (bz2, gz, and zip). From the root of the project you just released perform the
following steps:

# cd into the checkout directory in the maven target directory
cd target/checkout/

# run the build.sh file or the following command this will build the distributions on the checked
# out version that was just released
mvn clean javadoc:javadoc assembly:assembly

# once the build is complete you can cd into the target directory and find the three distribution
# files (flexjson-2.x.tar.bz2, flexjson-2.x.tar.gz, and flexjson-2.x.tar.zip).
cd target

# if you have made any changes to the flexjson website you can unzip one of the distributions and
# copy the docs folder up to the sourceforge website

== Additional Information ==

Actual preparation for setting up a Sonatype repository for syncing with the Maven Central
repository. This has already been handled but it is good information in order to understand the
bigger picture.

https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide