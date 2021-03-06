/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Gareth Jon Lynch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.gazbert.bxbot.exchanges;

import com.gazbert.bxbot.exchange.api.AuthenticationConfig;
import com.gazbert.bxbot.exchange.api.ExchangeAdapter;
import com.gazbert.bxbot.exchange.api.ExchangeConfig;
import com.gazbert.bxbot.exchange.api.NetworkConfig;
import com.gazbert.bxbot.exchange.api.OptionalConfig;
import com.gazbert.bxbot.trading.api.BalanceInfo;
import com.gazbert.bxbot.trading.api.MarketOrderBook;
import com.gazbert.bxbot.trading.api.Ticker;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Basic integration testing with Cryptopia exchange.
 *
 * @author nodueck
 */
@Ignore("18 May 2019 : Disabling test - the exchange has gone into liquidation: ")
public class CryptopiaIT {

    // Canned test data
    private static final String MARKET_ID = "btc_usdt";
    private static final BigDecimal SELL_ORDER_PRICE = new BigDecimal("10000.176");
    private static final BigDecimal SELL_ORDER_QUANTITY = new BigDecimal("0.01");

    // Exchange Adapter config for the tests
    private static final String PUBLIC_KEY = "key123";
    private static final String PRIVATE_KEY = "notGonnaTellYa";
    private static final List<Integer> nonFatalNetworkErrorCodes = Arrays.asList(502, 503, 504);
    private static final List<String> nonFatalNetworkErrorMessages = Arrays.asList(
            "Connection refused", "Connection reset", "Remote host closed connection during handshake");

    private ExchangeConfig exchangeConfig;
    private AuthenticationConfig authenticationConfig;
    private NetworkConfig networkConfig;
    private OptionalConfig optionalConfig;


    /*
     * Create some exchange config - the TradingEngine would normally do this.
     */
    @Before
    public void setupForEachTest() throws Exception {

    	optionalConfig = createMock(OptionalConfig.class);
    	expect(optionalConfig.getItem("use_global_trading_fee")).andReturn("false");
    	expect(optionalConfig.getItem("global_trading_fee")).andReturn("0.20");
    	
        authenticationConfig = createMock(AuthenticationConfig.class);
        expect(authenticationConfig.getItem("public_key")).andReturn(PUBLIC_KEY);
        expect(authenticationConfig.getItem("private_key")).andReturn(PRIVATE_KEY);

        networkConfig = createMock(NetworkConfig.class);
        expect(networkConfig.getConnectionTimeout()).andReturn(30);
        expect(networkConfig.getNonFatalErrorCodes()).andReturn(nonFatalNetworkErrorCodes);
        expect(networkConfig.getNonFatalErrorMessages()).andReturn(nonFatalNetworkErrorMessages);

        exchangeConfig = createMock(ExchangeConfig.class);
        expect(exchangeConfig.getAuthenticationConfig()).andReturn(authenticationConfig);
        expect(exchangeConfig.getNetworkConfig()).andReturn(networkConfig);
        expect(exchangeConfig.getOptionalConfig()).andReturn(optionalConfig);
        
    }

    @Test
    public void testPublicApiCalls() throws Exception {

        replay(authenticationConfig, networkConfig, optionalConfig, exchangeConfig);

        final ExchangeAdapter exchangeAdapter = new CryptopiaExchangeAdapter();
        exchangeAdapter.init(exchangeConfig);

        assertNotNull(exchangeAdapter.getLatestMarketPrice(MARKET_ID));
        assertNotNull(exchangeAdapter.getLatestMarketPrice(MARKET_ID));

        final MarketOrderBook orderBook = exchangeAdapter.getMarketOrders(MARKET_ID);
        assertFalse(orderBook.getBuyOrders().isEmpty());
        assertFalse(orderBook.getSellOrders().isEmpty());

        final Ticker ticker = exchangeAdapter.getTicker(MARKET_ID);
        assertNotNull(ticker.getLast());
        assertNotNull(ticker.getAsk());
        assertNotNull(ticker.getBid());
        assertNotNull(ticker.getHigh());
        assertNotNull(ticker.getLow());
        assertNotNull(ticker.getOpen());
        assertNotNull(ticker.getVolume());
        assertNull(ticker.getVwap()); // vwap not supplied by finex
        assertNotNull(ticker.getTimestamp());

        verify(authenticationConfig, networkConfig, exchangeConfig);
    }

    /*
     * You'll need to change the KEY, SECRET, constants to real-world values.
     */
    @Ignore("Disabled. Integration testing authenticated API calls requires your secret credentials!")
    @Test
    public void testAuthenticatedApiCalls() throws Exception {

        replay(authenticationConfig, networkConfig, optionalConfig, exchangeConfig);

        final ExchangeAdapter exchangeAdapter = new CryptopiaExchangeAdapter();
        exchangeAdapter.init(exchangeConfig);

        assertNotNull(exchangeAdapter.getPercentageOfBuyOrderTakenForExchangeFee(MARKET_ID));
        assertNotNull(exchangeAdapter.getPercentageOfSellOrderTakenForExchangeFee(MARKET_ID));

        final BalanceInfo balanceInfo = exchangeAdapter.getBalanceInfo();
        assertNotNull(balanceInfo.getBalancesAvailable().get("BTC"));

        // Careful here - make sure the SELL_ORDER_PRICE is sensible!
//        final String orderId = exchangeAdapter.createOrder(MARKET_ID, OrderType.SELL, SELL_ORDER_QUANTITY, SELL_ORDER_PRICE);
//        final List<OpenOrder> openOrders = exchangeAdapter.getYourOpenOrders(MARKET_ID);
//        assertTrue(openOrders.stream().anyMatch(o -> o.getId().equals(orderId)));
//        assertTrue(exchangeAdapter.cancelOrder(orderId, MARKET_ID));

        verify(authenticationConfig, networkConfig, exchangeConfig);
    }
}
