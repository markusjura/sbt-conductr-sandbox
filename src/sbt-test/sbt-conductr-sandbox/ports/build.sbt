import org.scalatest.Matchers._
import ByteConversions._

lazy val root = (project in file(".")).enablePlugins(ConductRSandbox)

name := "port-test"

version := "0.1.0-SNAPSHOT"

// ConductR bundle keys
BundleKeys.nrOfCpus := 1.0
BundleKeys.memory := 64.MiB
BundleKeys.diskSpace := 10.MB
BundleKeys.endpoints += "other" -> Endpoint("http", 0, Set(URI("http://:9001/other-service")))

// ConductR sandbox keys
SandboxKeys.ports in Global := Set(1111, 2222)
SandboxKeys.debugPort := 5432

val checkPorts = taskKey[Unit]("Check that the specified ports are exposed to docker.")

checkPorts := {
  val content = s"docker port cond-0".!!
  val expectedLines = Set(
    """9004/tcp -> 0.0.0.0:9004""",
    """9005/tcp -> 0.0.0.0:9005""",
    """9006/tcp -> 0.0.0.0:9006""",
    """1111/tcp -> 0.0.0.0:1101""",
    """2222/tcp -> 0.0.0.0:2202""",
    """5432/tcp -> 0.0.0.0:5402""",
    """9001/tcp -> 0.0.0.0:9001"""
  )

  expectedLines.foreach(line => content should include(line))
}

val checkDebugJVMArgument = taskKey[Unit]("Check that the debug port has been added as an JVM argument to the start-command")

checkDebugJVMArgument := {
  val contents = IO.read((target in Bundle).value / "tmp" / "bundle.conf")
  val expectedContents = """start-command    = ["port-test-0.1.0-SNAPSHOT/bin/port-test", "-J-Xms67108864", "-J-Xmx67108864", "-jvm-debug 5432"]""".stripMargin
  contents should include(expectedContents)
}