package com.debate.croll.youtubelive.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.debate.croll.youtubelive.Mapper.Id;
import com.debate.croll.youtubelive.Mapper.Item;
import com.debate.croll.youtubelive.Mapper.Snippet;
import com.debate.croll.youtubelive.Mapper.YouTubeSearchResponse;
import com.debate.croll.youtubelive.domain.YoutubeLive;
import com.debate.croll.youtubelive.domain.YoutubeLiveDto;
import com.debate.croll.youtubelive.infrastructure.YoutubeLiveEntity;
import com.debate.croll.youtubelive.infrastructure.YoutubeLiveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class KboLive {

	private final YoutubeLiveRepository youtubeLiveRepository;


	//@Scheduled(cron = "0 0 * * * ?")// 매 시 정각마다 실행
	//@Scheduled(fixedDelay = 500000000)
	@Transactional
	public void doCroll() throws JsonProcessingException {

		LocalDateTime now = LocalDateTime.now().withNano(0);

		// 오늘 오후 3시 기준 시간 만들기
		LocalDateTime today14pm = now.withHour(14).withMinute(0).withSecond(0).withNano(0);
		LocalDateTime today21pm = now.withHour(23).withMinute(0).withSecond(0).withNano(0);

		if((now.isAfter(today14pm) && now.isBefore(today21pm)) || now.isEqual(today14pm) || now.isEqual(today21pm)){ // 14pm ~ 21pm LCK 낮
			//
			activate();
		}else{
			//
			log.warn("KBO 상영 시간대 아님");
		}
	}



	private void update(YoutubeLiveEntity oldData, YoutubeLiveDto newData){

		oldData.setTitle(newData.getTitle());
		oldData.setSupplier(newData.getSupplier());
		oldData.setVideoId(newData.getVideoId());
		oldData.setCreatedAt(newData.getCreateAt());
	}

	//@Transactional
	private void activate() {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("part", "snippet");
			params.add("channelId", "UCoVz66yWHzVsXAFG8WhJK9g");
			params.add("eventType", "live");
			params.add("type", "video");
			params.add("key", "AIzaSyCdEG_MS81NpdlsAJOQwmzS21u7L_K-r0M"); // 보통 실제 서비스에서는 보안상 환경변수로 관리합니다.

			WebClient webClient =
				WebClient.builder()
					.baseUrl("https://www.googleapis.com")
					.build();

			String result = webClient
				.get()
				.uri(
					uriBuilder ->
						uriBuilder
							.path("/youtube/v3/search")
							.queryParam("maxResults", 1)
							.queryParams(params)
							.build()
				)
				.retrieve()
				.bodyToMono(String.class)
				.block();

			ObjectMapper mapper = new ObjectMapper();
			YouTubeSearchResponse response = mapper.readValue(result, YouTubeSearchResponse.class);

			// response -> items -> id -> videoId
			List<Item> items = response.getItems();

			Item item = items.get(0);
			Id id = item.getId();

			// fields

			// videoId
			String videoId = id.getVideoId(); // 1

			Snippet snippet = item.getSnippet();

			// 수정해야함!!!!!
			String createdAt = snippet.getPublishedAt(); // 2

			// 'Z' 제거
			if (createdAt.endsWith("Z")) {
				createdAt = createdAt.substring(0, createdAt.length() - 1);
			}

			// 파싱
			LocalDateTime localDateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

			String title = snippet.getTitle(); // 3
			//String content = snippet.getDescription(); // 4
			String supplier = snippet.getChannelTitle();

			String category = "kbo"; // 5

			// 새로운 youtubelive 데이터 생성하기
			YoutubeLive newYoutubeLive = YoutubeLive.builder()
				.videoId(videoId)
				.title(title)
				.supplier(supplier)
				.category(category)
				.createAt(localDateTime)
				.build();

			// 1. 가져오기
			YoutubeLiveEntity fetchedYoutubeLiveEntity = youtubeLiveRepository.fetch(category);
			YoutubeLiveDto youtubeLiveDto = newYoutubeLive.createDto();

			// 만약 null이면 -> 없음 -> 새로 넣어주자. 아니면 더티 체킹
			if (fetchedYoutubeLiveEntity == null) {
				youtubeLiveRepository.save(youtubeLiveDto);
			} else { // Dirty-Checking
				update(fetchedYoutubeLiveEntity, youtubeLiveDto);
			}
		}
		catch (IndexOutOfBoundsException e){
			log.warn("현재 KBO LIVE는 하지 않습니다.");
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		catch (WebClientResponseException e ){
			log.error("유튜브 API 할당량 모두 소진 -> KboLive");
		}

	}
}
