package de.hsosnabrueck.verteiltesysteme.views

import de.hsosnabrueck.verteiltesysteme.MQEmpfaenger
import de.hsosnabrueck.verteiltesysteme.Styles
import de.hsosnabrueck.verteiltesysteme.WetterAenderung
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.scene.chart.PieChart
import javafx.scene.control.Alert.AlertType.INFORMATION
import tornadofx.*

class MainView : View("Hello TornadoFX") {
    private val daten: ObservableList<PieChart.Data> = FXCollections.observableArrayList()
    private val ddaten: ObservableSet<PieChart.Data> = FXCollections.observableSet()

    init {
        MQEmpfaenger("192.168.0.151", "wetterStatistik", this::wetterGeaendert)
    }

    private fun wetterGeaendert(wetterAenderung: WetterAenderung) {
        daten.add(
                PieChart.Data(wetterAenderung.status, wetterAenderung.tweets.toDouble())
        )
    }

    override val root = borderpane {
        addClass(Styles.welcomeScreen)
        piechart {
            data = daten
        }
    }


}