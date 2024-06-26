import scala.collection.Seq

val tapirVersion = "1.10.4"
val sparkVersion = "3.5.1"
ThisBuild / scalacOptions += "-Wunused:all"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name           := "lsf-metrics",
    version        := "0.1.0-SNAPSHOT",
    organization   := "fr.rebaze",
    scalaVersion   := "3.4.1",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"      % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics"   % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"    % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"             % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-zio-prelude"          % tapirVersion,
      "ch.qos.logback"                 % "logback-classic"            % "1.5.6",
      "dev.zio"                       %% "zio-logging"                % "2.2.2",
      "dev.zio"                       %% "zio-logging-slf4j"          % "2.2.2",
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"     % tapirVersion % Test,
      "dev.zio"                       %% "zio-test"                   % "2.0.22"     % Test,
      "dev.zio"                       %% "zio-test-sbt"               % "2.0.22"     % Test,
      "dev.zio"                       %% "zio-config-typesafe"        % "4.0.1",
      "dev.zio"                       %% "zio-config-magnolia"        % "4.0.1",
      "com.softwaremill.sttp.client3" %% "zio-json"                   % "3.9.5"      % Test,
      "dev.zio"                       %% "zio-nio"                    % "2.0.2",
      ("io.getquill"                  %% "quill-jdbc-zio"             % "4.8.3").exclude("org.scala-lang.modules", "scala-parallel-collections_3"),
      ("org.scala-lang.modules"       %% "scala-parallel-collections" % "1.0.4"      % "provided").cross(CrossVersion.for3Use2_13),
      "org.postgresql"                 % "postgresql"                 % "42.7.3",
      ("org.apache.spark"             %% "spark-sql"                  % sparkVersion % "provided").cross(CrossVersion.for3Use2_13),
      ("org.apache.spark"             %% "spark-core"                 % sparkVersion).cross(CrossVersion.for3Use2_13),
      // ("com.github.pureconfig"        %% "pureconfig"                 % "0.17.1").cross(CrossVersion.for3Use2_13),
      // "org.apache.hadoop"              % "hadoop-client"              % "3.4.0"      % Provided,
      "io.github.vincenzobaz"         %% "spark-scala3-encoders"      % "0.2.6",
      "io.github.vincenzobaz"         %% "spark-scala3-udf"           % "0.2.6",
      "io.github.vincenzobaz"         %% "spark-scala3"               % "0.1.5",
      "io.univalence"                 %% "zio-spark"                  % "0.12.0",
      "org.apache.hadoop"              % "hadoop-client"              % "3.4.0"      % Provided
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  ),
  javaOptions ++= Seq("--add-exports=java.base/sun.nio.ch=ALL-UNNAMED"),
  fork := true
)
