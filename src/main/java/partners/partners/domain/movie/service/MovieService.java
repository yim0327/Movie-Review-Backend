package partners.partners.domain.movie.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import partners.partners.domain.movie.dto.request.AddMovieRequest;
import partners.partners.domain.movie.dto.request.FixMovieRequest;
import partners.partners.domain.movie.dto.response.MovieResponse;
import partners.partners.domain.movie.entity.Movie;
import partners.partners.domain.movie.repository.MovieRepository;
import partners.partners.exception.MovieNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;

    @Transactional
    public void createMovie(AddMovieRequest addMovieRequest){
        Movie movie = addMovieRequest.toSaveMovie();
        movieRepository.save(movie);
    }
    @Transactional
    public void soft_deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("영화가 없습니다."));
        movieRepository.delete(movie);
    }
    @Transactional
    public void updateMovie(Long id, FixMovieRequest fixMovieRequest){
        Movie movie = movieRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new MovieNotFoundException("영화가 없습니다."));
        Movie updatedMovie = Movie.builder()
                .id(movie.getId()) // 기존 ID 유지
                .title(fixMovieRequest.getTitle()) // 제목 수정
                .genre(fixMovieRequest.getGenre()) // 장르 수정
                .openDate(fixMovieRequest.getOpenDate()) // 개봉일 수정
                .closeDate(fixMovieRequest.getCloseDate()) // 종영일 수정
                .isCurrentlyShowing(fixMovieRequest.getIsCurrentlyShowing()) // 상영중 여부 수정
                .createdAt(movie.getCreatedAt()) // 생성일자는 그대로 두기
                .updatedAt(LocalDateTime.now()) // 수정된 시간 기록
                .isDeleted(movie.getIsDeleted()) // 삭제 여부 그대로 유지
                .build();
        movieRepository.save(updatedMovie);
    }

    @Transactional
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new MovieNotFoundException("영화가 없습니다."));
        return MovieResponse.fromEntity(movie);
    }

    @Transactional
    public List<MovieResponse> searchMovies(String genre, Boolean isShowing) {
        List<Movie> movies = movieRepository.findAllByIsDeletedFalse();

        if (genre != null) {
            movies = movies.stream()
                    .filter(movie -> movie.getGenre().name().equalsIgnoreCase(genre))
                    .collect(Collectors.toList());
        }

        if (isShowing != null) {
            movies = movies.stream()
                    .filter(movie -> movie.isCurrentlyShowing() == isShowing)
                    .collect(Collectors.toList());
        }

        return movies.stream()
                .sorted((m1, m2) -> m1.getOpenDate().compareTo(m2.getOpenDate()))
                .map(MovieResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
