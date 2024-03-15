plugins {
    id("app.cash.sqldelight") version "2.0.1"
}

sqldelight {
    databases {
        create("LudoDB") {
            packageName.set("net.kodein.powerludo.db")
            srcDirs.setFrom("$projectDir/src@sqldelight")
        }
    }
}