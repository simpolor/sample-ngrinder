package io.simpolor.ngrinder

import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.plugin.http.HTTPRequest
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import net.grinder.util.GrinderUtils
import org.codehaus.groovy.reflection.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.util.concurrent.ThreadLocalRandom

import static net.grinder.script.Grinder.grinder
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

/**
 * A simple example using the HTTP plugin that shows the retrieval of a
 * single page via HTTP.
 *
 * This script is automatically generated by ngrinder.
 *
 * @author admin
 */
@RunWith(GrinderRunner)
class SampleProperties {

    public static GTest test
    public static HTTPRequest request
    public static NVPair[] headers = []
    public static NVPair[] params = []
    public static Cookie[] cookies = []

    static def samples
    def currentSample

    static {
        def props = new Properties()
        def clazz = ReflectionUtils.getCallingClass(0)

        def student = clazz.getResourceAsStream('/sample2.properties');

        props.load student;
        def config = new ConfigSlurper().parse props;
        student.close();

        samples = config.sample.data.trim()?.tokenize(',') ?: ['1|MinsuKim|18|Soccer','2|James|19|Run'];

    }

    @BeforeProcess
    static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 6000
        test = new GTest(1, "Test1")
        request = new HTTPRequest()
        grinder.logger.info("before process.")
    }

    @BeforeThread
    void beforeThread() {

        // 로그레벨 설정
        // LoggerFactory.getLogger("worker").setLevel(Level.DEBUG)
        // LoggerFactory.getLogger(JUnitThreadContextInitializer.class).setLevel(Level.DEBUG)

        test.record(this, "test")
        grinder.statistics.delayReports=true
        grinder.logger.info("before thread.")

        // Thread 증가
        def uid = threadUID(samples) // sample 데이터
        currentSample = samples[uid].toString().split(/\|/)

    }

    @Before
    void before() {
        request.setHeaders(headers)
        cookies.each { CookieModule.addCookie(it, HTTPPluginControl.getThreadHTTPClientContext()) }
        grinder.logger.info("before thread. init headers and cookies")
    }

    @Test
    void test(){

        println "currentSample : "+currentSample

        HTTPResponse result = request.GET("https://www.naver.com/", params)

        if (result.statusCode == 301 || result.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", result.statusCode);
        } else {
            assertThat(result.statusCode, is(200));
        }
    }

    // 쓰레드별 유니크아이디
    def threadUID = { source ->
        try {
            return GrinderUtils.threadUniqId
        } catch (NullPointerException e) { // 로컬 테스트시 발생
            def no = randomInt source.size()
            grinder.logger.info("\n### Unique Thread Number] no: ${no} / error: ${e.toString()}\n")
            return no
        }
    }

    // thread 별 랜덤 숫자 리턴; least는 포함, bound는 미포함
    def randomInt = { bound, least=0 -> ThreadLocalRandom.current().nextInt(least as int, bound as int) }


}
