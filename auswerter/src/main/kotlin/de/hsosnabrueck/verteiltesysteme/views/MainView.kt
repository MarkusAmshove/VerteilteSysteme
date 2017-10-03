package de.hsosnabrueck.verteiltesysteme.views

import de.hsosnabrueck.verteiltesysteme.MQEmpfaenger
import de.hsosnabrueck.verteiltesysteme.Styles
import de.hsosnabrueck.verteiltesysteme.WetterAenderung
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.PieChart
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.layout.GridPane
import tornadofx.*

class MainView : View("Hello TornadoFX") {
    private val daten: ObservableList<PieChart.Data> = FXCollections.observableArrayList()
    override val root = GridPane()

    private var chart: PieChart? = null

    init {
        with(root) {
            row {
                chart = piechart("Imported Fruits") {
                    data("Rain", 25.0)
                    data
                }
            }
        }
        MQEmpfaenger("192.168.0.151", "wetterStatistik", this::wetterGeaendert)
    }

    private fun wetterGeaendert(wetterAenderung: WetterAenderung) {
        if (chart == null) return
        if (chart!!.data == null) return

        for (i in chart!!.data.indices) {
            if (chart!!.data[i].name == wetterAenderung.status) {
                chart!!.data[i].pieValue = wetterAenderung.tweets.toDouble()
                return
            }
        }
        chart!!.data.add(PieChart.Data(wetterAenderung.status, wetterAenderung.tweets.toDouble()))
    }


}
