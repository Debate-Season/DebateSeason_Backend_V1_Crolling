package com.debate.croll.scheduler;

import java.time.LocalDateTime;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.debate.croll.domain.YoutubeRepository;
import com.debate.croll.domain.entity.Youtube;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class YoutubeScheduler {

	private final YoutubeRepository youtubeRepository;

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

		// 1.
		WebElement element1 = driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-browse/ytd-two-column-browse-results-renderer/div[1]/ytd-section-list-renderer/div[2]/ytd-item-section-renderer[1]/div[3]/ytd-shelf-renderer/div[1]/div[2]/ytd-expanded-shelf-contents-renderer/div/ytd-video-renderer[1]/div[1]/div/div[1]/div/h3/a"));
		String herf1 = element1.getAttribute("href");
		String title1 = element1.getAttribute("title");
		Youtube youtube1 = Youtube.builder()
			.url(herf1)
			.title(title1)
			.createdAt(now)
			.media("youtube")
			.build()
			;

		//
		WebElement element2 = driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-browse/ytd-two-column-browse-results-renderer/div[1]/ytd-section-list-renderer/div[2]/ytd-item-section-renderer[1]/div[3]/ytd-shelf-renderer/div[1]/div[2]/ytd-expanded-shelf-contents-renderer/div/ytd-video-renderer[2]/div[1]/div/div[1]/div/h3/a"));
		String herf2 = element2.getAttribute("href");
		String title2 = element2.getAttribute("title");
		Youtube youtube2 = Youtube.builder()
			.url(herf2)
			.title(title2)
			.createdAt(now)
			.media("youtube")
			.build()
			;


		WebElement element3 = driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-browse/ytd-two-column-browse-results-renderer/div[1]/ytd-section-list-renderer/div[2]/ytd-item-section-renderer[3]/div[3]/ytd-shelf-renderer/div[1]/div[2]/ytd-expanded-shelf-contents-renderer/div/ytd-video-renderer[1]/div[1]/div/div[1]/div/h3/a"));
		String herf3 = element3.getAttribute("href");
		String title3 = element3.getAttribute("title");
		Youtube youtube3 = Youtube.builder()
			.url(herf3)
			.title(title3)
			.createdAt(now)
			.media("youtube")
			.build()
			;


		WebElement element4 = driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-browse/ytd-two-column-browse-results-renderer/div[1]/ytd-section-list-renderer/div[2]/ytd-item-section-renderer[3]/div[3]/ytd-shelf-renderer/div[1]/div[2]/ytd-expanded-shelf-contents-renderer/div/ytd-video-renderer[2]/div[1]/div/div[1]/div/h3/a"));
		String herf4 = element4.getAttribute("href");
		String title4 = element4.getAttribute("title");
		Youtube youtube4 = Youtube.builder()
			.url(herf4)
			.title(title4)
			.createdAt(now)
			.media("youtube")
			.build()
			;


		WebElement element5 = driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-browse/ytd-two-column-browse-results-renderer/div[1]/ytd-section-list-renderer/div[2]/ytd-item-section-renderer[3]/div[3]/ytd-shelf-renderer/div[1]/div[2]/ytd-expanded-shelf-contents-renderer/div/ytd-video-renderer[3]/div[1]/div/div[1]/div/h3/a"));
		String herf5 = element5.getAttribute("href");
		String title5 = element5.getAttribute("title");
		Youtube youtube5 = Youtube.builder()
			.url(herf5)
			.title(title5)
			.createdAt(now)
			.media("youtube")
			.build()
			;

		youtubeRepository.save(youtube1);
		youtubeRepository.save(youtube2);
		youtubeRepository.save(youtube3);
		youtubeRepository.save(youtube4);
		youtubeRepository.save(youtube5);

	driver.quit();


	}
}
