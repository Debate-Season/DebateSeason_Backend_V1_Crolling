package com.debate.croll.scheduler.community;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class FmKorea {

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


		// 시간순으로 셔플하자
		// LocalDateTime now = LocalDateTime.now().withNano(0);
		// 분전(min),시간전(hour),일전(day)


		WebDriver driver = new ChromeDriver(options);

		// 정치/시사 + 인기
		driver.get("https://www.fmkorea.com/index.php?mid=politics&sort_index=pop&order_type=desc");

		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(1) > div > h3 > a  // 제목 링크
		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(1) > div > div:nth-child(5) > span.regdate // 시간

		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(2) > div > h3 > a
		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(2) > div > div:nth-child(5) > span.regdate

		// 오늘 YYYY-MM-DD
		LocalDate today = LocalDate.now();

		for(int i = 1 ; i<= 5 ; i++){
			WebElement titleElement = driver.findElement(By.cssSelector("#bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child("+i+") > div > h3 > a"));
			WebElement timeElement = driver.findElement(By.cssSelector("#bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child("+i+") > div > div:nth-child(5) > span.regdate"));

			String timeString = today + " " + timeElement.getText();

			// Create a DateTimeFormatter with the appropriate pattern
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

			// Parse the string to a LocalDateTime object
			LocalDateTime dateTime = LocalDateTime.parse(timeString, formatter);

			Media fmKorea = Media.builder()
				.title(titleElement.getText())
				.url(titleElement.getAttribute("href"))
				.media("에펨코리아")
				.category("정치")
				.type("community")
				.createdAt(dateTime)
				.build()
				;

			mediaRepository.save(fmKorea);
		}

		driver.quit();

	}
}
