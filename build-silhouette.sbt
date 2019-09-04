resolvers += "iheartradio-maven" at "https://dl.bintray.com/iheartradio/maven"
resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"

libraryDependencies ++= {
  val silhouetteVersion = "6.1.0"
  Seq(
    "com.mohiva" %% "play-silhouette" % silhouetteVersion,
    "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
    "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
    "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
    "com.mohiva" %% "play-silhouette-testkit" % silhouetteVersion % "test",
    "com.iheart" %% "ficus" % "1.4.7",
    "net.codingwell" %% "scala-guice" % "4.2.6",
    specs2 % Test,
    ehcache,
    guice
  )
}
