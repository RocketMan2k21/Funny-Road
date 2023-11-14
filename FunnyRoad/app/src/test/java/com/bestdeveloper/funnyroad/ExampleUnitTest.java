package com.bestdeveloper.funnyroad;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.android.gms.maps.model.LatLng;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void measuringRouteDistance_isCorrect(){
        LatLng firstPoint = new LatLng(50.83806237588368, 25.453695453079334);
        LatLng secondPoint = new LatLng(50.83425848791794, 25.467587756165145);

        RoadGenerator roadGeneratorTest = new RoadGenerator(null, null, null, 0);

    }


}