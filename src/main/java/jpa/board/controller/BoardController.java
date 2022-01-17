package jpa.board.controller;

import jpa.board.domain.Board;
import jpa.board.domain.dto.BoardCreateDto;
import jpa.board.domain.dto.BoardEditDto;
import jpa.board.exception.NotExistException;
import jpa.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
@Slf4j
public class BoardController {
//    private final BoardRepository boardRepository;
    private final BoardService boardService;

    @GetMapping
    public String boards(Model model) {
        List<Board> boards = boardService.findAllBoard();
        model.addAttribute("boards", boards);

        return "/board/boardList";
    }

    @GetMapping("/new-board")
    public String createBoardForm(Model model) {
        model.addAttribute("boardCreateDto", new BoardCreateDto());

        return "/board/boardCreateForm";
    }

    @PostMapping("/new-board")
    public String createBoard(@Valid @ModelAttribute BoardCreateDto boardCreateDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasFieldErrors()) {
            List<ObjectError> allErrors = bindingResult.getAllErrors();

            allErrors.forEach((error) -> log.error("Error Arguments : {}, Error Message : {}", error.getArguments(), error.getDefaultMessage()));

            return "/board/boardCreateForm";
        }

        Board board = new Board();
        Board newBoard = board.createBoard(boardCreateDto);

        Long boardId = boardService.createBoard(newBoard);

        redirectAttributes.addAttribute("boardId", boardId);

        return "redirect:/boards/{boardId}";
    }

    @GetMapping("/{boardId}")
    public String readBoard(@PathVariable("boardId") Long boardId, Model model) {
        try {
            Board findBoard = boardService.findBoard(boardId);

            model.addAttribute("board", findBoard);
        } catch (NotExistException e) {
            e.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return "/board/board";
    }

    @GetMapping("/{boardId}/edit")
    public String editBoardForm(@PathVariable("boardId") Long boardId, Model model) {
        Board board = boardService.findBoard(boardId);
        BoardEditDto boardEditDto = BoardEditDto.convertToBoardEditDto(board);

        model.addAttribute("boardEditDto", boardEditDto);

        return "/board/boardEditForm";
    }

    @DeleteMapping("/boards/{boardId}/delete")
    public String deleteBoard(@PathVariable("boardId") Long boardId, HttpEntity<String> entity) {
        log.info("body - {}", entity.getBody());

        boardService.deleteBoard(boardId);

        return "redirect:/boards";
    }

    @GetMapping("/best-board")
    public String bestBoard() {

        return "/board/bestBoard";
    }
}
