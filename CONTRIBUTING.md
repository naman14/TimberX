# Contributing to TimberX

We obviously love your input! We want to make contributing to this project as easy and transparent as possible, whether it's:

- Reporting a bug

- Discussing the current state of the code

- Submitting a fix

- Proposing new features

- Becoming a maintainer

## We always develop with Github

We use github to host code, to track issues and feature requests, as well as accept pull requests.

## We make use of [Github Flow](https://guides.github.com/introduction/flow/index.html), So all Code Changes happen through Pull Requests

Pull requests are the best way to propose changes to the existing codebase (we use [Github Flow](https://guides.github.com/introduction/flow/index.html)). We actively welcome your pull requests:

1. Fork the repo and create your branch from `master`.

2. If you've added code that should be tested, add tests.

3. If you've changed APIs (we currently make use of Last.FM), update the documentation accordingly.

4. Ensure the test suite passes.

5. All the styling stuff would be covered by the fact that you have to run `./gradlew spotlessApply` in order for the build to pass (which runs a formatter along with ktlint).

6. Make sure your code lints are okay (if you have run `./gradlew spotlessApply` as stated above, then you need not worry about this).

7. Issue that pull request!

## Any contributions you make will be under the GNU General Public License

In short, when you submit code changes, your submissions are understood to be under the same [GNU General Public License](https://www.gnu.org/licenses/) that covers this project. Feel free to contact the maintainers if the choice of license is going to be a concern.

## Report bugs using Github's [issues](https://github.com/naman14/TimberX/issues)

We use GitHub issues to track public bugs. Report a bug by [opening a new issue](https://github.com/naman14/TimberX/issues/new?template=Bug_report.md); it's that easy!

## Write bug reports with detail, background, and sample code

[This is an example](https://stackoverflow.com/q/48849725/7354463) of a bug report I wrote, and I think it's not a bad model. Here's [another example from Craig Hockenberry](http://www.openradar.me/11905408), an app developer whom I greatly respect.

**Great Bug Reports** tend to have:

- A quick summary and/or background

- Steps to reproduce

  - Be specific!

  - Give sample code if you can. [My stackoverflow question](https://stackoverflow.com/q/53482699/7354463) includes sample code that *anyone* with a base R setup can run to reproduce what I was seeing

- What you expected would happen

- What actually happens

- Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)

People *love* thorough bug reports. I'm not even kidding.

We continually make use of GitHub issues to also track feature requests. Request a feature by [opening a new issue](https://github.com/naman14/TimberX/issues/new?template=Feature_request.md); it's that easy!

## Use a Consistent Coding Style

* 2 spaces for indentation rather than tabs

* You can try running `npm run lint` for style unification

## License

By contributing, you agree that your contributions will be licensed under its GNU General Public License.

## References

This document was adapted from the open-source contribution guidelines for [Facebook's Draft](https://github.com/facebook/draft-js/blob/a9316a723f9e918afde44dea68b5f9f39b7d9b00/CONTRIBUTING.md)
