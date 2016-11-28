# ![graphvizzer_logo](readme_resources/graphvizzer_logo.png "logo") Graphvizzer <a href="https://slack.com/oauth/authorize?scope=commands&client_id=29667068068.63519026177"><img alt="Add to Slack" height="40" width="139" src="https://platform.slack-edge.com/img/add_to_slack.png" srcset="https://platform.slack-edge.com/img/add_to_slack.png 1x, https://platform.slack-edge.com/img/add_to_slack@2x.png 2x" /></a>

[![Docker Pulls](https://img.shields.io/docker/pulls/objectivetruth/graphvizzer.svg)](https://hub.docker.com/r/objectivetruth/graphvizzer/)
[![CircleCI](https://img.shields.io/circleci/project/ObjectiveTruth/Graphvizzer.svg)](https://circleci.com/gh/ObjectiveTruth/Graphvizzer/tree/master)
[![Coverage Master Branch](https://img.shields.io/codecov/c/github/ObjectiveTruth/Graphvizzer/master.svg)](https://codecov.io/gh/ObjectiveTruth/Graphvizzer/branch/master)

Makes graphs with imgur links directly in your chat channel, instantly, using the popular [DOT](http://www.graphviz.org/content/dot-language) notation. 

View the production version at [https://graphvizzer.objectivetruth.ca]()

View the stage version at [http://graphvizzer.stage.objectivetruth.ca]()

![Example Usage](readme_resources/graphviz_demo.gif "Example Usage")

## History

I love diagram.. as you can see from this repo. Software is created by people and as the team grows, communication becomes more and more difficult. I use `Graphviz` alot to communicate ideas and reduce ambiguity. I hope you'll find it helpful too!

*replacing the word warfare with software*

>Fleet Marine Force Manual 1, Warfighting 
>
“Software is a complex endeavor. It is shaped by
the human will. It is characterized by friction,
uncertainty, ﬂuidity, danger and disorder. While the
nature of software is constant, it remains
unpredictable, and is affected by a mix of physical,
moral and mental factors. While software has the
characteristics of both art and science, it is
primarily shaped by human experience”.

## Architecture

Uses [Play2! Scala](https://www.playframework.com/) to communicate between [imgur](http://imgur.com/) and [Slack](https://slack.com/)

### Deployment Diagram

![Deployment Diagram](readme_resources/DeploymentDiagram.jpeg "Deployment Diagram")

### Interaction Diagram

![Interaction Diagram](readme_resources/InteractionDiagram.jpeg "Interaction Diagram")

## Project Setup (Nodejs required)

Change to the directory `/csci3230-project` and download dependencies with `npm install`

### Run local server

`npm start`

### Integration tests

`npm test`

### Building the docker

Must be in the project root directory

1. Build the `Docker` image

  `docker build -t objectivetruth/graphvizzer .`
  
2. Run the `Docker` image

  `docker run -p 9000:9000 objectivetruth/graphvizzer`
  
View at `localhost:9000` in your browser

## Project Setup (Scala)

[IntelliJ](https://www.jetbrains.com/idea/) is the recommended way to edit the project

Simply import the project based on an `sbt` model to get started

### Integration Tests

`activator test`

## License

[GNU General Public License v3.0](http://choosealicense.com/licenses/gpl-3.0/#)


