package com.github.abaker.googlevoice

import Voice
import com.github.abaker.googlevoice.proto.parseData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxParsingTest {
    @Test
    fun emptyInbox() {
        val payload = """
            [null,null,"v1-1-1704468812369389"]
        """.trimIndent()
        val response = parseData(Voice.ListResponse::class.java, payload)
        assertTrue(response.threadList.isEmpty())
        assertFalse(response.hasPaginationToken())
        assertEquals("v1-1-1704468812369389", response.versionToken)
    }

    @Test
    fun singleIncomingSMS() {
        val payload = """
            [[["t.+13123806253",0,[["86dcd623b1b1594b4b4b547d66854848c8002bf9",1704468888454,"+17736694020",["+13123806253","+13123806253",null,null,null,null,0],10,0,null,null,null,"Hello world",null,null,5,1,null,"+13123806253",0,null,[null,1]]],null,[["+13123806253","+13123806253",null,null,null,null,0]],[1,2],null,1,["+13123806253"],null,null,[]]],null,"v1-1-1704468889938708"]
        """.trimIndent()
        val response = parseData(Voice.ListResponse::class.java, payload)
        val threads = response.threadList
        assertEquals(1, threads.size)
        val messages = threads.first().messagesList
        assertEquals(1, messages.size)
        assertEquals("Hello world", messages.first().messageText)
    }

    @Test
    fun singleIncomingGroupText() {
        val payload = """
            [[["g.Group Message.BEDHgNA/2uGtUp6W9ube/Q",0,[["166007d448169e510e6e1da18779bf9157eac297",1704493017063,"+17736694020",["Group Message.BEDHgNA/2uGtUp6W9ube/Q","Group Message.BEDHgNA/2uGtUp6W9ube/Q"],10,0,null,null,null,"+13123806253 - Group text message",null,null,5,1,["Group text message","",null,[["+13123806253","+13123806253",null,null,null,null,0],["+16032053438","+16032053438",null,null,null,null,0]],"+13123806253",["+13123806253","+16032053438"]],"Group Message.BEDHgNA/2uGtUp6W9ube/Q",0,null,[null,1]]],null,[["+16032053438","+16032053438",null,null,null,null,0],["Group Message.BEDHgNA/2uGtUp6W9ube/Q","Group Message.BEDHgNA/2uGtUp6W9ube/Q"],["+13123806253","+13123806253",null,null,null,null,0]],[1,2],null,1,["+13123806253","+16032053438"],null,null,[]]],null,"v1-1-1704493019470353"]
        """.trimIndent()
        val response = parseData(Voice.ListResponse::class.java, payload)

    }

    @Test
    fun moreMessages() {
        val payload = """
            [[["g.Group Message.BEDHgNA/2uGtUp6W9ube/Q",1,[["a52c027907c12358a97671a03aa1bce6d370738b",1704503978268,"+17736694020",["Group Message.BEDHgNA/2uGtUp6W9ube/Q","Group Message.BEDHgNA/2uGtUp6W9ube/Q"],11,1,null,null,null,"Yo!",null,null,6,1,["Yo!","",null,[["+13123806253","+13123806253",null,null,null,null,0],["+16032053438","+16032053438",null,null,null,null,0]],null,["+13123806253","+16032053438"]],"Group Message.BEDHgNA/2uGtUp6W9ube/Q",0,"154314583120842",[null,1]],["166007d448169e510e6e1da18779bf9157eac297",1704493017063,"+17736694020",["Group Message.BEDHgNA/2uGtUp6W9ube/Q","Group Message.BEDHgNA/2uGtUp6W9ube/Q"],10,1,null,null,null,"+13123806253 - Group text message",null,null,5,1,["Group text message","",null,[["+13123806253","+13123806253",null,null,null,null,0],["+16032053438","+16032053438",null,null,null,null,0]],"+13123806253",["+13123806253","+16032053438"]],"Group Message.BEDHgNA/2uGtUp6W9ube/Q",0,null,[null,1]]],null,[["+16032053438","+16032053438",null,null,null,null,0],["Group Message.BEDHgNA/2uGtUp6W9ube/Q","Group Message.BEDHgNA/2uGtUp6W9ube/Q"],["+13123806253","+13123806253",null,null,null,null,0]],[1,2],null,1,["+13123806253","+16032053438"],null,null,[]],["t.+13123806253",1,[["a5606b4fad98ce5e0cc9218969d678e3ba2f7128",1704503974957,"+17736694020",["+13123806253","+13123806253",null,null,null,null,0],11,1,null,null,null,"Hey!",null,null,6,1,null,"+13123806253",0,"218306504692001",[null,1]],["8317c933e4b44bf6eb84d85f37ecf1c14ab2d492",1704503747993,"+17736694020",["+13123806253","+13123806253",null,null,null,null,0],10,1,null,null,null,"Hello world!",null,null,5,1,null,"+13123806253",0,null,[null,1]]],null,[["+13123806253","+13123806253",null,null,null,null,0]],[1,2],null,1,["+13123806253"],null,null,[]]],null,"v1-1-1704503978641925"]
        """.trimIndent()
        parseData(Voice.ListResponse::class.java, payload)
    }

    @Test
    fun handleEscapedStringInText() {
        val payload = """
            [[["t.68382",0,[["7c595e96431e388f35a999218a6472ad4508b3b1",1703436902495,"+13123806253",["68382","68382",null,null,null,null,0],10,1,null,null,null,"Walmart: your Alien - 7\" Xenomorph Dron... was delivered. Details: https://w-mt.co/g/32XYYF Reply HELP for info; STOP to opt out",null,null,5,1,null,"68382",0,null,[null,1]]],null,[["68382","68382",null,null,null,null,0]],[1,2],null,1,["68382"],null,null,[]]],"1703628607906","v1-1-1704642552802635"]
        """.trimIndent()
        val response = parseData(Voice.ListResponse::class.java, payload)
        assertEquals(
            "Walmart: your Alien - 7\\\" Xenomorph Dron... was delivered. Details: https://w-mt.co/g/32XYYF Reply HELP for info; STOP to opt out",
            response.threadList.first().messagesList.first().messageText
        )
    }

    @Test
    fun handleCommaInText() {
        val payload = """
            [[["t.68382",0,[["7c595e96431e388f35a999218a6472ad4508b3b1",1703436902495,"+13123806253",["68382","68382",null,null,null,null,0],10,1,null,null,null,"this is a test, with a comma",null,null,5,1,null,"68382",0,null,[null,1]]],null,[["68382","68382",null,null,null,null,0]],[1,2],null,1,["68382"],null,null,[]]],"1703628607906","v1-1-1704642552802635"]
        """.trimIndent()
        val response = parseData(Voice.ListResponse::class.java, payload)
        assertEquals(
            "this is a test, with a comma",
            response.threadList.first().messagesList.first().messageText
        )
    }
}