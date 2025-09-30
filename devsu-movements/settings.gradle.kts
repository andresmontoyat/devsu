rootProject.name = "devsu-movements"

include(":application")
project(":application").projectDir = file("./application")

include(":bootstrap")
project(":bootstrap").projectDir = file("./bootstrap")

include(":domain")
project(":domain").projectDir = file("./domain")

include(":jpa-repository")
project(":jpa-repository").projectDir = file("./infrastructure/driven-adapters/jpa-repository")

include(":kafka-producer")
project(":kafka-producer").projectDir = file("./infrastructure/driven-adapters/kafka-producer")

include(":security")
project(":security").projectDir = file("./infrastructure/driven-adapters/security")

include(":rest-controller")
project(":rest-controller").projectDir = file("./infrastructure/entry-points/rest-controller")

include(":kafka-consumer")
project(":kafka-consumer").projectDir = file("./infrastructure/entry-points/kafka-consumer")
