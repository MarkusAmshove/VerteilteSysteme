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
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import tornadofx.*

class MainView : View("Hello TornadoFX") {
    private val daten: ObservableList<PieChart.Data> = FXCollections.observableArrayList()
    override val root = GridPane()

    private var chart: PieChart? = null

    private lateinit var labelPanel: StackPane

    init {
        with(root) {
            row {
                chart = piechart("Imported Fruits") {
                    data("Rain", 25.0)
                    data
                }
            }
            row {
                labelPanel = stackpane { }
            }
        }
        MQEmpfaenger("192.168.1.1", "wetterStatistik", this::wetterGeaendert)
    }

    private fun wetterGeaendert(wetterAenderung: WetterAenderung) {
        if (chart == null) return
        if (chart!!.data == null) return

        for (i in chart!!.data.indices) {
            if (chart!!.data[i].name == wetterAenderung.status) {
                chart!!.data[i].pieValue = wetterAenderung.tweets.toDouble()
                updateLabels()
                return
            }
        }
        chart!!.data.add(PieChart.Data(wetterAenderung.status, wetterAenderung.tweets.toDouble()))
        updateLabels()
    }

    private fun updateLabels() {
        labelPanel.children.clear()
        for (data in chart!!.data) {
            labelPanel.children.add(
                    Label("${data.name}: ${data.pieValue}")
            )
        }
    }


}
