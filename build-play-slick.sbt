  libraryDependencies ++= {
  val PLAY_SLICK_VERSION = "4.0.2"
  Seq(
    "com.typesafe.play" %% "play-slick" % PLAY_SLICK_VERSION,
    "com.typesafe.play" %% "play-slick-evolutions" % PLAY_SLICK_VERSION
  )
}
