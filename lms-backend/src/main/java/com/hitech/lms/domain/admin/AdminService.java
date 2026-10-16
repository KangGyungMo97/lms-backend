package com.hitech.lms.domain.admin;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.domain.user.Dept;
import com.hitech.lms.domain.user.SmsService;
import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.DeptRepository;
import com.hitech.lms.domain.user.Users;
import com.hitech.lms.domain.user.UsersRepository;
import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final AdminPostRepository apr;
	private final UsersRepository ur;
	private final DeptRepository dr;
	private final UserStatusLogRepository uslr;
	private final UserService us;
	private final SmsService sms;                     // „ì‹œ ë¹„ëë²ˆí˜¸ ë¬¸ì ë°œì†¡
	private final SecureRandom random = new SecureRandom();

	// ===== Œì› =====

	// Œì› ëª©ë¡ - ˜ì´ì§+ ê²€(´ë¦„, ™ë²ˆ) + „í„° (êµ¬ë¶„, ™ê³¼, íƒœ)
	public Page<Users> getUserList(int page, String kw, String role, String deptId, String status) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "userCreated"));
		return ur.search(role, deptId, status, kw.trim(), pageable);
	}

	// ™ê³¼¤ë§ˆ¸ì†Œ„íŠ¸¨ì–´ê³˜ë‚˜ë¡ê³ ì • (€ ê²°ì •)
	public static final String DEPT_ID = "D001";

	// ê³„ì • ±ë¡ (ìŠ¹ - …ë ¥ ¤ë¥˜IllegalStateException("„ë“œëªë©”ì‹œì§€") ¼ë¡œ Œë ¤ ì¤
	@Transactional
	public Users createUser(UserCreateForm f) {
	    String userId = f.getUserId().trim();
	    String email = f.getUserEmail().trim();
		String role   = f.getUserRole();
		String grade  = f.getUserGrade();

		if (!f.isTempPwIssued()) {
			throw new IllegalStateException("tempPwIssued:[„ì‹œ ë¹„ëë²ˆí˜¸ ë°œê¸‰] ë²„íŠ¼ŒëŸ¬ ì£¼ì„¸");
		}
		if (!List.of("S", "P", "A").contains(role)) {
			throw new IllegalStateException("userRole:êµ¬ë¶„ ê°’ì´ ¬ë°”ë¥´ì ŠìŠµˆë‹¤.");
		}
		if ("S".equals(role) && !List.of("1", "2").contains(grade)) {
			throw new IllegalStateException("userGrade:™ìƒ€ ™ë…„(1 Â· 2™ë…„) íƒì£¼ì„¸");
		}
	    if (ur.existsById(userId)) {
	        throw new IllegalStateException("userId:´ë ±ë¡™ë²ˆ/¬ë²ˆ…ë‹ˆ");
	    }
	    if (ur.existsByUserEmail(email)) {
	        throw new IllegalStateException("userEmail:´ë ¬ìš© ì¤‘ì¸ ´ë©”¼ì…ˆë‹¤.");
	    }
		if (ur.existsById(userId)) {
			throw new IllegalStateException("userId:´ë ±ë¡™ë²ˆ/¬ë²ˆ…ë‹ˆ");
		}
		if (ur.existsByUserEmail(email)) {
			throw new IllegalStateException("userEmail:´ë ¬ìš© ì¤‘ì¸ ´ë©”¼ì…ˆë‹¤.");
		}
		// „ì‹œ ë¹„ëë²ˆí˜¸: œë²„ê°€ „ì˜ë¡ë§Œë“¤ê³ ”í˜¸”í•´€ê´€ë¦¬ì†ìŒ
		String tempPw = makeTempPassword();
		Users user = us.create(userId, f.getUserName().trim(), tempPw, email,
				f.getUserTel().trim(), f.getUserBday(), DEPT_ID, role);

		// ™ë…„€ ™ìƒë§(êµìˆ˜ Â· ê´€ë¦¬ìNULL)
		user.setUserGrade("S".equals(role) ? grade : null);
		ur.save(user);
	    
		// ê³„ì • ì£¼ì¸´ë°ìœ¼ë¡œë§Œ „ì‹œ ë¹„ëë²ˆí˜¸ ë°œì†¡ (ê°œë°œ ì¤‘ì—LogSmsService ê°€ œë²„ ì½˜ì†”ì¶œë ¥)
		sms.send(user.getUserTel(), "[˜ì´Œí¬ LMS] " + user.getUserName() + "ê³„ì •±ë¡˜ì—ˆµë‹ˆ "
				+ "„ì´ " + user.getUserId() + " / „ì‹œ ë¹„ëë²ˆí˜¸: " + tempPw
				+ " (ë¡œê·¸ë¹„ëë²ˆí˜¸ ì°¾ê¸°ì„œ ë¹„ëë²ˆí˜¸ë¡ë°”ê¿” ì£¼ì„¸");
		return user;
	}

	// „ì‹œ ë¹„ëë²ˆí˜¸ ë§Œë“¤ê¸- ë¬¸ €Œë¬¸+ «ì + ¹ìˆ˜ë¬¸ì ì–´10(·ê°ˆë¦¬ëŠ” 0 O 1 l I œì™¸)
	private String makeTempPassword() {
		String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ", lower = "abcdefghijkmnopqrstuvwxyz",
				digit = "23456789", special = "!@#$%^&*";
		String all = upper + lower + digit + special;
		List<Character> chars = new ArrayList<>();
		chars.add(upper.charAt(random.nextInt(upper.length())));      // ì¢…ë¥˜ë§ˆë‹¤ ìµœì†Œ 1ê°
		chars.add(lower.charAt(random.nextInt(lower.length())));
		chars.add(digit.charAt(random.nextInt(digit.length())));
		chars.add(special.charAt(random.nextInt(special.length())));
		while (chars.size() < 10) chars.add(all.charAt(random.nextInt(all.length())));
		Collections.shuffle(chars, random);
		StringBuilder sb = new StringBuilder();
		chars.forEach(sb::append);
		return sb.toString();
	}

	// ê³ ì • ™ê³¼ ´ë¦„ (”ë©´ œì‹œ
	public String getFixedDeptName() {
		return dr.findById(DEPT_ID).map(Dept::getDeptName).orElse("¤ë§ˆ¸ì†Œ„íŠ¸¨ì–´ê³);

	    // Œë¦¼ ë¬¸ì ë°œì†¡
	    sms.send(user.getUserTel(), "[˜ì´Œí¬ LMS] " + user.getUserName() + "ê³„ì •±ë¡˜ì—ˆµë‹ˆ " +
	            "„ì´ " + user.getUserId() + " / „ì‹œ ë¹„ëë²ˆí˜¸: " + tempPw + " (ë¡œê·¸ë¹„ëë²ˆí˜¸ ì°¾ê¸°ì„œ ë¹„ëë²ˆí˜¸ë¡ë°”ê¿” ì£¼ì„¸");

	    return user;
	}

	// „ì‹œ ë¹„ëë²ˆí˜¸ ë§Œë“¤ê¸- ë¬¸ €Œë¬¸+ «ì + ¹ìˆ˜ë¬¸ì ì–´10(·ê°ˆë¦¬ëŠ” 0 O 1 l I œì™¸)
	private String makeTempPassword() {
		String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ", lower = "abcdefghijkmnopqrstuvwxyz",
				digit = "23456789", special = "!@#$%^&*";
		String all = upper + lower + digit + special;
		List<Character> chars = new ArrayList<>();
		chars.add(upper.charAt(random.nextInt(upper.length())));      // ì¢…ë¥˜ë§ˆë‹¤ ìµœì†Œ 1ê°
		chars.add(lower.charAt(random.nextInt(lower.length())));
		chars.add(digit.charAt(random.nextInt(digit.length())));
		chars.add(special.charAt(random.nextInt(special.length())));
		while (chars.size() < 10) chars.add(all.charAt(random.nextInt(all.length())));
		Collections.shuffle(chars, random);
		StringBuilder sb = new StringBuilder();
		chars.forEach(sb::append);
		return sb.toString();
	}

	// ê³ ì • ™ê³¼ ´ë¦„ (”ë©´ œì‹œ
	public String getFixedDeptName() {
		return dr.findById(DEPT_ID).map(Dept::getDeptName).orElse("¤ë§ˆ¸ì†Œ„íŠ¸¨ì–´ê³);
	}

	// µê³„ ì¹´ë“œ: „ì²´ / ™ìƒ / êµìˆ˜ / ì°¨ë‹¨Â· ê¸ˆ
	public Map<String, Long> getUserStats() {
		return Map.of(
			"total",   ur.count(),
			"student", ur.countByUserRole("S"),
			"teacher", ur.countByUserRole("P"),
			"blocked", ur.countByUserStatusIn(List.of("L", "B"))
		);
	}

	// íƒœ ë³€ê²(•ìƒ /  ê¸ˆ / ì°¨ë‹¨ / ˆí‡´) + ´ë ¥ ê¸°ë¡
	@Transactional
	public void changeStatus(Users user, String status, String reason,
	                         LocalDate blockStart, LocalDate blockEnd, String adminId) {

		if (!List.of("A", "L", "B", "W").contains(status)) {
			throw new IllegalArgumentException("íƒœ ê°’ì´ ¬ë°”ë¥´ì ŠìŠµˆë‹¤.");
		}
		if (user.getUserId().equals(adminId)) {
			throw new IllegalArgumentException("ë³¸ì¸ ê³„ì •íƒœë³€ê²½í•  †ìŠµˆë‹¤.");
		}
		if ("W".equals(user.getUserStatus())) {
			throw new IllegalArgumentException("ˆí‡´ ì²˜ë¦¬ê³„ì •€ íƒœë¥ë³€ê²½í•  †ìŠµˆë‹¤.");
		}
		if (status.equals(user.getUserStatus())) {
			throw new IllegalArgumentException("´ë ´ë‹¹ íƒœ…ë‹ˆ");
		}
		if (reason == null || reason.isBlank()) {
			throw new IllegalArgumentException("¬ìœ „ìˆ˜ ª©…ë‹ˆ");
		}
		if ("B".equals(status)) {
			if (blockStart == null || blockEnd == null) {
				throw new IllegalArgumentException("ì°¨ë‹¨ ê¸°ê°„…ë ¥ì£¼ì„¸");
			}
			if (blockEnd.isBefore(blockStart)) {
				throw new IllegalArgumentException("ì°¨ë‹¨ ì¢…ë£Œ¼ì œì‘¼ë³´ë¹ ë †ìŠµˆë‹¤.");
			}
		} else {
			blockStart = null;
			blockEnd = null;
		}

		UserStatusLog log = new UserStatusLog();
		log.setUser(user);
		log.setBeforeStatus(user.getUserStatus());
		log.setAfterStatus(status);
		log.setReason(reason.trim());
		log.setBlockStart(blockStart);
		log.setBlockEnd(blockEnd);
		log.setChangedAt(LocalDateTime.now());
		log.setChangedBy(adminId);
		uslr.save(log);

		user.setUserStatus(status);
		ur.save(user);
	}

	// íƒœ ë³€ê²´ë ¥ (ìµœì‹ 
	public List<UserStatusLog> getStatusLogs(Users user) {
		return uslr.findByUserOrderByChangedAtDesc(user);
	}

	// ===== ê´€ë¦¬ì ê²Œì‹œê¸€ (œìŠ¤ê³µì) =====

	// ëª©ë¡ - ìµœì‹ + ˜ì´ì§10ê° + ê²€œëª© Â· ´ìš©)
	public Page<AdminPost> getPostList(int page, String kw) {
		page = Math.max(page, 0);
		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "adminPostId"));
		return apr.findPostList(kw.trim(), pageable);
	}

	// ê°ì°¾ê¸° - †ê±° œ(N)ê¸€´ë©´ DataNotFoundException(404)
	public AdminPost getPost(Long id) {
		AdminPost post = apr.findById(id)
				.orElseThrow(() -> new DataNotFoundException("ê²Œì‹œê¸€ì°¾ì„ †ìŠµˆë‹¤."));
		if (!"Y".equals(post.getAdminPostIsUse())) {
			throw new DataNotFoundException("ê²Œì‹œê¸€ì°¾ì„ †ìŠµˆë‹¤.");
		}
		return post;
	}

	// ì¡°íšŒ+1
	@Transactional
	public void increaseViewCount(AdminPost adminPost) {
		adminPost.setAdminPostView(adminPost.getAdminPostView() + 1);
		apr.save(adminPost);
	}

	// ±ë¡ - ‘ì„±= ë¡œê·¸¸í•œ ê´€ë¦¬ì
	@Transactional
	public AdminPost createPost(String title, String content, String adminId) {
		AdminPost post = new AdminPost();
		post.setAdminPostTitle(title.trim());
		post.setAdminPostContent(content);
		post.setAdminPostCreateDate(LocalDate.now());
		post.setAdminPostUpdateDate(LocalDate.now());
		post.setWriter(us.getUser(adminId));
		return apr.save(post);
	}

	// ˜ì • - ˜ì •now
	@Transactional
	public void modifyPost(AdminPost post, String title, String content) {
		post.setAdminPostTitle(title.trim());
		post.setAdminPostContent(content);
		post.setAdminPostUpdateDate(LocalDate.now());
		apr.save(post);
	}

	//  œ - ìœ ê²Œì‹œê³¼ ê°™ì ë°©ì‹ (admin_post_is_use = 'N')
	@Transactional
	public void deletePost(AdminPost post) {
		post.setAdminPostIsUse("N");
		apr.save(post);
	}

	// ===== ™ê³¼ ë°ê²€ì¦ í‹¸ =====

	// ™ê³¼ ëª©ë¡
	public List<Dept> getDeptList() {
		return dr.findAll();
	}
	
	// ™ë²ˆ/¬ë²ˆ ì¤‘ë³µ •ì¸
	public boolean isUserIdTaken(String userId) {
		return ur.existsById(userId);
	}

	// ´ë©”ì¤‘ë³µ •ì¸
	public boolean isUserEmailTaken(String email) {
		return ur.existsByUserEmail(email.trim());
	}

	// „í™”ë²ˆí˜¸ ˜ì´ë™ ë³€˜ê¸°
	private String normalizeTel(String tel) {
		if (tel == null) return null;
		String d = tel.replaceAll("\\D", "");
		if (d.length() == 11) return d.substring(0, 3) + "-" + d.substring(3, 7) + "-" + d.substring(7);
		if (d.length() == 10) return d.substring(0, 3) + "-" + d.substring(3, 6) + "-" + d.substring(6);
		return tel;
	}
}
