val tapirVersion = "1.10.4"
ThisBuild / scalacOptions += "-Wunused:all"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name           := "lsf-metrics",
    version        := "0.1.0-SNAPSHOT",
    organization   := "fr.rebaze",
    scalaVersion   := "3.4.1",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"    % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"  % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"           % tapirVersion,
      "ch.qos.logback"                 % "logback-classic"          % "1.5.5",
      "dev.zio"                       %% "zio-logging"              % "2.1.15",
      "dev.zio"                       %% "zio-logging-slf4j"        % "2.1.15",
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"   % tapirVersion % Test,
      "dev.zio"                       %% "zio-test"                 % "2.0.13"     % Test,
      "dev.zio"                       %% "zio-test-sbt"             % "2.0.13"     % Test,
      "com.softwaremill.sttp.client3" %% "zio-json"                 % "3.9.5"      % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
)
