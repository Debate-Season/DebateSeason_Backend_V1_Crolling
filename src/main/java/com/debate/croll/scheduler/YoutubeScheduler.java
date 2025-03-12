package com.debate.croll.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.debate.croll.domain.entity.Media;
import com.debate.croll.repository.MediaRepository;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class YoutubeScheduler {

	private final MediaRepository mediaRepository;

	@Scheduled(cron = "0 0 17 * * ?",zone = "Asia/Seoul")
	public synchronized void doCroll() throws InterruptedException {
		WebDriverManager.chromedriver().setup();


		// 랜덤 User-Agent 리스트
		String[] userAgents = {
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
		};

		// 랜덤 User-Agent 선택
		Random rand = new Random();
		String userAgent = userAgents[rand.nextInt(userAgents.length)];

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new"); // headless 모드
		options.addArguments("user-agent=" + userAgent); // 랜덤 User-Agent 설정

		// 추가적인 헤더 설정 (필요시)
		options.addArguments("--disable-blink-features=AutomationControlled"); // 이거 false 나와야 자동화 회피 가능하다
		options.addArguments("--disable-gpu"); // GPU 비활성화 (성능 개선)

		LocalDateTime now = LocalDateTime.now().withNano(0);// 현재 크롤링 시간

		WebDriver driver = new ChromeDriver(options);

		driver.get("https://www.youtube.com/feed/trending");



		// 컨테이너 통째로 가져오는 법 찾아 볼 것
		List<WebElement> elementList = driver.findElements(By.cssSelector("#grid-container"));



		for(WebElement e : elementList){
			WebElement webElement = e.findElement(By.cssSelector("#video-title"));

			String media = e.findElement(By.cssSelector("#text > a")).getText();

			Media youtube = Media.builder()
				.title(webElement.getAttribute("title"))
				.type("youtube")
				.url(webElement.getAttribute("href"))
				.createdAt(now)
				.media(media)
				.category("사회")
				.build()
				;
			mediaRepository.save(youtube);
		}
		driver.quit();
	}
}
