# Contributing to the a4c-yorc-api-plugin project

**First off**, thanks for taking time to contribute!

The following is a set of guidelines for contributing to a4c-yorc-api-plugin.
Feel free to provide feedback about it in an
issue or pull request.

Don't be afraid to contribute, if something is unclear then just ask or submit the issue or pull request
anyways. The worst that can happen is that you'll be politely asked to change something.

## How to make a release

Releasing is reserved to members with push permission.

Everything is automated using a [github action workflow named `Release`](https://github.com/ystia/a4c-yorc-api-plugin/actions?query=workflow%3ARelease).
You have to click on the `Run workflow` dropdown menu select the release branch and click the `Run workflow` button.

Running this workflow with default (empty) inputs is generally fine (maven release plugin will take care of selecting version numbers)
but we can also specify the version to be released and the next development version (should end with `-SNAPSHOT`).

This workflow will use maven release plugin to tag a version and prepare the next development cycle, use again maven release plugin to upload
tagged artifacts to the Maven Github Packages Repository, draft a new release or update the existing draft with the plugin artifact and finally publish the release.
