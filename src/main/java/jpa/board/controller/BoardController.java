package jpa.board.controller;

import jpa.board.domain.Board;
import jpa.board.domain.Comment;
import jpa.board.domain.dto.BoardCreateDto;
import jpa.board.domain.dto.BoardEditDto;
import jpa.board.domain.dto.CommentCreateDto;
import jpa.board.exception.NotExistException;
import jpa.board.repository.BoardRepository;
import jpa.board.service.BoardService;
import jpa.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
@Slf4j
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;

    @GetMapping
    public String boards(Model model, @PageableDefault(size = 12, sort = "id", direction = DESC) Pageable pageable, @RequestParam(value = "property", required = false) String property) {
        Page<Board> boardList = boardService.findAllBoard(pageable);
        log.info("page {}", boardList.getNumber());
        model.addAttribute("boardList", boardList);

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

        return "redirect:/boards/{boardId}/comments";
    }

    @GetMapping("/{boardId}/comments")
    public String readBoard(@PathVariable("boardId") Long boardId, @PageableDefault(size = 10, sort = "id", direction = DESC) Pageable pageable, Model model) {
        try {
            Board board = boardService.findBoard(boardId);
            Slice<Comment> comments = commentService.findComments(boardId, pageable);
            boardService.addViews(board);

            model.addAttribute("board", board);
            model.addAttribute("comments", comments);
            model.addAttribute("commentCreateDto", new CommentCreateDto());
        } catch (NotExistException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "/board/board";
    }

    @GetMapping("/{boardId}/edit")
    public String editBoardForm(@PathVariable("boardId") Long boardId, Model model) {
        log.info("edit boardId : {}", boardId);

        Board board = boardService.findBoard(boardId);
        BoardEditDto boardEditDto = BoardEditDto.convertToBoardEditDto(board);

        model.addAttribute("boardEditDto", boardEditDto);

        return "/board/boardEditForm";
    }

    @PostMapping("/{boardId}/edit")
    public String editBoard(@PathVariable("boardId") Long boardId, @Validated @ModelAttribute("boardEditDto") BoardEditDto boardEditDto, BindingResult bindingResult) {
        if(bindingResult.hasFieldErrors()) {
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            allErrors.forEach((error) -> log.error("Error Arguments : {}, Error Message : {}", error.getArguments(), error.getDefaultMessage()));

            return "/board/boardEditForm";
        }
        boardService.editBoard(boardId, boardEditDto);

        return "redirect:/boards/{boardId}/comments";
    }

    @PostMapping("/{boardId}/delete")
    public String deleteBoard(@PathVariable("boardId") Long boardId) {
        log.info("delete id : {}", boardId);

        boardService.deleteBoard(boardId);

        return "redirect:/boards";
    }

    @GetMapping("/best-board")
    public String bestBoard() {

        return "/board/bestBoard";
    }

    @PostMapping("/{boardId}/new-comment")
    public String createComment(@PathVariable Long boardId, @Validated @ModelAttribute CommentCreateDto commentCreateDto, BindingResult bindingResult, Model model, @PageableDefault(size = 10, sort = "id", direction = DESC) Pageable pageable) {
        if(bindingResult.hasErrors()) {
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            allErrors.forEach((error) -> log.error("Error Arguments : {}, Error Message : {}", error.getArguments(), error.getDefaultMessage()));

            model.addAttribute("board", boardService.findBoard(boardId));
            model.addAttribute("comments", commentService.findComments(boardId, pageable));
            return "/board/board";
        }
        Comment comment = new Comment();
        Comment newComment = comment.createComment(commentCreateDto);

        commentService.createComment(boardId, newComment);

        return "redirect:/boards/{boardId}/comments";
    }
}
