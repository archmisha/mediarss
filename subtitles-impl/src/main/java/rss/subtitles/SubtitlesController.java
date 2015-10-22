//package rss.subtitles;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//import rss.MediaRSSException;
//import rss.dao.SubtitlesDao;
//import rss.torrents.Subtitles;
//
//import javax.servlet.ServletOutputStream;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// * User: dikmanm
// * Date: 10/02/13 18:16
// */
//@Controller
//@RequestMapping("/subtitles")
//public class SubtitlesController {
//
//	@Autowired
//	private SubtitlesDao subtitlesDao;
//
//	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/zip")
//	@ResponseBody
//	public void get(@PathVariable long id, HttpServletResponse response) {
//		Subtitles subtitles = subtitlesDao.find(id);
//
//		try {
//			response.setContentType("application/zip");
//			response.setHeader("Content-Disposition", "attachment; filename=" + subtitles.getName());
//
//			ServletOutputStream out = response.getOutputStream();
//			out.write(subtitles.getData());
//			out.flush();
//			out.close();
//		} catch (IOException e) {
//			throw new MediaRSSException("Failed to get subtitles: " + e.getMessage(), e);
//		}
//	}
//}