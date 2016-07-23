package com.objectivetruth.graphvizslackapp.models

case class SlashCommandIn(
                           token: String,
                           teamId: String,
                           teamDomain: String,
                           channelId: String,
                           channelName: String,
                           userId: String,
                           userName: String,
                           command: String,
                           text: String,
                           responseUrl: String
                         )

