package main

import (
	"fmt"
	"encoding/json"
	"io/ioutil"
	//"net/http"
	"math"
	"time"
)

type FoodOrder struct {
	Name string `json:"name"`
	Temp string `json:"temp"`
	ShelfLife int `json:"shelfLife"`
	DecayRate float64 `json:"decayRate"`
}

// for the Poisson calculations
//
const PI=3.1415926535897932384626433832795

// These values are not magical, just the default values Marsaglia used.
// Any unit should work
var m_u uint32 = 521288629
var m_v uint32 = 362436069

var OrderData []FoodOrder
var OrderIndex int = 0

func loadOrders(jsonFile string) ([]FoodOrder, error) {
	content, err := ioutil.ReadFile(jsonFile)
	if err != nil {
		fmt.Println("loadOrders: Failed to read orders file:", err)
		return nil, err
	}

	orderData := make([]FoodOrder,0)
	err = json.Unmarshal(content, &orderData)
	if err != nil {
		fmt.Println("loadOrders: Failed to unmarshal orders file:", err)
		return nil, err
	}

	return orderData, nil
}

func sendOrder() bool {
	fmt.Println("sendOrder: index=", OrderIndex)
	if OrderIndex >= len(OrderData) {
		fmt.Println("sendOrder: done")
		return false
	}

	order := OrderData[OrderIndex]
	fmt.Printf("sendOrder: send: type=%T val=%v\n", order, order)

	OrderIndex = OrderIndex + 1
	return true
}

// I adapted this function from www.johndcook.com/SimpleRNG.cpp
// Algo impl in c++ he derived from Knuth
func poisson(lambda float64) int {
	//case <-time.After(time.Minute):
	//return 325

	// Algorithm due to Donald Knuth, 1969.
	//double p = 1.0, L = exp(-lambda);
	p := 1.0
	L := math.Exp(-lambda);
	k := 0;
	//do {
	for {
		k++
		//p *= GetUniform();
		m_v = 36969*(m_v & 65535) + (m_v >> 16)
		m_u = 18000*(m_u & 65535) + (m_u >> 16)
		z := (m_v << 16) + m_u
		// The magic number is 1/(2^32 + 1) and so result is positive and less than 1.
		mn := math.Exp2(32)
		mn = 1 / (mn + 1)
		z2 := float64(z) * mn // 2.328306435996595e-10
		p *= z2

		if p <= L {
			break
		}
	}
	//while (p > L);
	return k - 1;
}

func calcPoissonVals(cnt int) []int {
	pvals := make([]int, cnt)
	for i := 0; i < cnt; i++ {
		pvals[i] = poisson(3.25)
		fmt.Println("calcPoissonVals: i=", i, " : v=", pvals[i])
	}
	return pvals
}

func main() {

	orderData, err := loadOrders("./orderdata.json")
	if err != nil {
		fmt.Println("ERROR: client: couldnt load order data: err=", err)
		return
	}
	OrderData = orderData

	// send the orders 1 at a time to the server, should be an average poisson rate of
	// 3.25 orders per second - we will precalculate the poisson range to use
	// in the loop below
	pvals := calcPoissonVals(len(OrderData))

	for _,pwait := range pvals {
		fmt.Println("loop: pwait=", pwait)
		twait := 1000.0 // wait a full second if the poisson val is 0
		if pwait > 0 {
			// divide up the second(1000 millis) by poisson val
			twait = 1000 / float64(pwait) // will wait this long before sending req
		} else {
			pwait = 1 // loop once to wait for a full second
		}
		fmt.Println("loop: twait=", twait)
		// loop thru the second and send poisson number of requests
		for i := 0; i < pwait; i++ {
			select {
				case <-time.After(time.Duration(float64(twait) * float64(time.Millisecond))):
					fmt.Println("Time after: try sendorder")
					if sendOrder() == false {
						return
					}
			}
		}
	}
}
