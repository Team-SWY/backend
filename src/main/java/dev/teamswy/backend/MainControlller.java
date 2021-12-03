package dev.teamswy.backend;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.teamswy.backend.dto.ChapterDTO;
import dev.teamswy.backend.dto.ProspectiveDTO;
import dev.teamswy.backend.dto.RoleDTO;
import dev.teamswy.backend.entity.Chapter;
import dev.teamswy.backend.entity.FraternityHQ;
import dev.teamswy.backend.entity.Member;
import dev.teamswy.backend.entity.Prospective_Member;
import dev.teamswy.backend.entity.Role;
import dev.teamswy.backend.entity.Statement;
import dev.teamswy.backend.repository.IChapterRepository;
import dev.teamswy.backend.repository.IFraternityHQRepository;
import dev.teamswy.backend.repository.IMemberRepository;
import dev.teamswy.backend.repository.IProspectRepository;
import dev.teamswy.backend.repository.IRoleRepository;
import dev.teamswy.backend.repository.IStatementRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@RestController
public class MainControlller {
    @Autowired
    private IChapterRepository chapterRepository;

    @Autowired
    private IFraternityHQRepository fraternityHQRepository;

    @Autowired
    private IMemberRepository memberRepository;

    @Autowired
    private IStatementRepository statementRepository;

    @Autowired
    private IProspectRepository prospectRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @GetMapping(value="/")
    @ResponseBody
    public ResponseEntity<Void> sendViaResponseEntity() {
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }
    
    @GetMapping(value="/chapter")
    public ResponseEntity<Iterable<Chapter>> getAllChapters() {
        Iterable<Chapter> chapters = chapterRepository.findAll();
        return new ResponseEntity<>(chapters, HttpStatus.OK);
    }

    @PostMapping("/chapter/create")
    public ResponseEntity<Chapter> createChapter(@RequestBody ChapterDTO chapter) {
        //Check for existing chapter name
        Optional<Chapter> existingChapter = chapterRepository.findByChapterName(chapter.getChapterName());
        if (existingChapter.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Chapter newChapter = new Chapter();
        newChapter.setChapterName(chapter.getChapterName());
        Optional<FraternityHQ> fHq = fraternityHQRepository.findById("ΦΚΨ");
        if(fHq.isPresent()) {
            newChapter.setChapterHQ(fHq.get());
        }
        long chapterId = chapterRepository.count();
        newChapter.setChapterId((int) (chapterId + 1));
        newChapter.setCharterDate(LocalDate.now());
        newChapter.setChapterStatus("Colony");
        newChapter.setChapterMembers(0);
        chapterRepository.save(newChapter);
        return new ResponseEntity<>(newChapter, HttpStatus.OK);
    }
    

    @GetMapping(value="/chapter/{chapterId}")
    public ResponseEntity<Chapter> getChapterById(@PathVariable(value="chapterId") int chapterId) {
        Optional<Chapter> chapter = chapterRepository.findById(chapterId);
        if(chapter.isPresent()) {
            return new ResponseEntity<>(chapter.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value="/chapter/{chapterId}/members")
    public ResponseEntity<Iterable<Member>> getChapterMembers(@PathVariable(value="chapterId") int chapterId) {
        Optional<Chapter> chapter = chapterRepository.findById(chapterId);
        if(chapter.isPresent()) {
            Iterable<Member> members = memberRepository.findByChapter(chapter.get());
            return new ResponseEntity<>(members, HttpStatus.OK);
            
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @GetMapping(value="/chapter/{chapterId}/prospects")
    public ResponseEntity<Iterable<Prospective_Member>> getChapterProspects(@PathVariable(value="chapterId") int chapterId) {
        Optional<Chapter> chapter = chapterRepository.findById(chapterId);
        if(chapter.isPresent()) {
            Iterable<Prospective_Member> prospects = prospectRepository.findByChapter(chapter.get());
            return new ResponseEntity<>(prospects, HttpStatus.OK);
            
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @PostMapping(value="/chapter/{chapterId}/prospects/add")
    public ResponseEntity<Prospective_Member> addProspect(@PathVariable(value="chapterId") int chapterId, @RequestBody ProspectiveDTO prospect) {
        Optional<Chapter> chapter = chapterRepository.findById(chapterId);
        if(chapter.isPresent()) {
            if(prospect.getPhone() != null && prospect.getPhone().length() > 0) {
                Optional<Prospective_Member> prospectExists = prospectRepository.findById(prospect.getPhone());
                if(prospectExists.isPresent()) {
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }
            }
            Prospective_Member prospectMember = new Prospective_Member();
            prospectMember.setName(prospect.getName());
            prospectMember.setEmail(prospect.getEmail());
            prospectMember.setPhone(prospect.getPhone());
            prospectMember.setBidchapter(chapter.get());
            prospectRepository.save(prospectMember);
            return new ResponseEntity<>(prospectMember,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @PostMapping(value="/chapter/{chapterId}/prospects/update")
    public ResponseEntity<Prospective_Member> updateProspect(@PathVariable(value="chapterId") int chapterId, @RequestBody ProspectiveDTO prospect) {
        Optional<Chapter> chapter = chapterRepository.findById(chapterId);
        if(chapter.isPresent()) {
            Optional<Prospective_Member> prospectMember = prospectRepository.findById(prospect.getPhone());
            if(prospectMember.isPresent()) {
                prospectMember.get().setName(prospect.getName());
                prospectMember.get().setEmail(prospect.getEmail());
                prospectMember.get().setPhone(prospect.getPhone());
                prospectRepository.save(prospectMember.get());
                return new ResponseEntity<>(prospectMember.get(),HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @PostMapping(value="/chapter/{chapterId}/prospects/delete")
    public ResponseEntity<Prospective_Member> deleteProspect(@PathVariable(value="chapterId") int chapterId, @RequestBody ProspectiveDTO deleteProspect) {
        Optional<Chapter> chapter = chapterRepository.findById(chapterId);
        Optional<Prospective_Member> prospect = prospectRepository.findById(deleteProspect.getPhone());
        if(chapter.isPresent() && prospect.isPresent()) {
            Prospective_Member prospectMember = prospect.get();
            prospectRepository.delete(prospectMember);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value="/chapter/{chapterId}/extendbid/")
    public ResponseEntity<String> extendBid(@PathVariable(value="chapterId") int chapterId, @RequestBody ProspectiveDTO extendBid) {
        Optional<Chapter> chapter = chapterRepository.findById(chapterId);
        Optional<Prospective_Member> prospect = prospectRepository.findById(extendBid.getPhone());
        if(chapter.isPresent() && prospect.isPresent()) {
            Prospective_Member prospectMember = prospect.get();
            prospectRepository.delete(prospectMember);
            Member member = new Member();
            Chapter currentChapter = chapter.get();
            long count = memberRepository.count();
            member.setName(prospectMember.getName());
            member.setEmail(prospectMember.getEmail());
            member.setPhone(prospectMember.getPhone());
            member.setChapter(currentChapter);
            member.setStatus("New Member");
            member.setInductionDate(LocalDate.now());
            member.setChapterMember(new ChapterMember(currentChapter.getChapterId(), (int) (count+1)));
            currentChapter.setChapterMembers(currentChapter.getChapterMembers()+1);
            memberRepository.save(member);
            chapterRepository.save(currentChapter);
            return new ResponseEntity<>("Bid Extended! Welcome to the fraternity.",HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value="/chapter/{chapterId}/roles")
    public ResponseEntity<Iterable<Role>> getChapterRoles(@PathVariable(value="chapterId") int chapterId) {
        Optional<Chapter> chapter = chapterRepository.findById(chapterId);
        if(chapter.isPresent()) {
            Iterable<Role> roles = roleRepository.findByChapter(chapter.get());
            return new ResponseEntity<>(roles, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value="/chapter/{chapterId}/roles/add")
    public ResponseEntity<Role> addRole(@PathVariable(value="chapterId") int chapterId, @RequestBody RoleDTO role) {
        Optional<Chapter> chapter = chapterRepository.findById(chapterId);
        if(chapter.isPresent()) {
            Optional<Role> roleExists = roleRepository.findById(role.getRankID());
            if (roleExists.isPresent()) {
                roleRepository.delete(roleExists.get());
            }
            Optional<Member> member = memberRepository.findById(new ChapterMember(chapter.get().getChapterId(), role.getRankID()));
            if(member.isPresent()) {
                Role newRole = new Role();
                newRole.setMember(member.get());
                newRole.setTitle(role.getTitle());
                newRole.setRank(role.getRankID());
                newRole.setExeuctiveBoard(role.isExeuctiveBoard());
                newRole.setStartDate(LocalDate.now());
                roleRepository.save(newRole);
                return new ResponseEntity<>(newRole, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value="/chapter/{chapterId}/statements")
    public ResponseEntity<Iterable<Statement>> getChapterStatements(@PathVariable(value="chapterId") int chapterId) {
        Optional<Chapter> chapter = chapterRepository.findById(chapterId);
        if(chapter.isPresent()) {
            Iterable<Statement> statements = statementRepository.findByChapter(chapter.get());
            return new ResponseEntity<>(statements, HttpStatus.OK);
            
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
