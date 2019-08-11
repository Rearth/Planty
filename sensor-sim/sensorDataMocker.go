package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"math/rand"
	"net/http"
	"os"
	"strconv"
	"time"
)

func main() {

	waterLoss, err := strconv.ParseFloat(os.Args[1], 32)
	waterLevel, err2 := strconv.ParseFloat(os.Args[2], 32)
	sensorName := os.Args[3]
	plantID, err3 := strconv.Atoi(os.Args[4])
	serverAdress := os.Args[5]

	if err != nil || err2 != nil || err3 != nil || len(os.Args) < 6 {
		print("error parsing arguements, usage: go run sensorDataMocker.go waterLoss waterLevel sensorName plantId serverAddr")
	}

	waterAdds := 0

	for {
		waterLevel -= waterLoss * (0.5 * rand.Float64())

		if waterAdds > 0 {
			waterLevel += (1 - waterLevel) * 0.3
			waterAdds--
			println("adding water!")
		}

		println("starting new request!")
		makeRequest(sensorName, waterLevel, plantID, serverAdress)
		time.Sleep(5 * time.Second)

		//start watering the flower simulation
		if rand.Float32() < 0.01 && waterAdds == 0 {
			//generate random int between 0 and 7
			waterAdds = rand.Intn(5) + 3
		}

	}

}

func makeRequest(sensorName string, waterLevel float64, plantID int, serverAdress string) {
	uri := fmt.Sprintf("%s/api/waterevent?sensorName=%s&plantID=%d&waterLevel=%.2f", serverAdress, sensorName, plantID, waterLevel)
	println(uri)
	resp, err := http.Get(uri)
	if err != nil {
		log.Println(err)
		return
	}

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Println(err)
		return
	}

	log.Println(string(body))
}
