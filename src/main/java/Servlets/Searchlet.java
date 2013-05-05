package Servlets;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import query.IndexQuery;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * Created by User: ting
 * Date: 4/28/13
 * Time: 4:41 PM
 */
public class Searchlet extends HttpServlet{
    private Directory index;
    /**
     * 
     * @param config  servelet config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        try{
            String path = config.getServletContext().getRealPath("/WEB-INF");
            index = new NIOFSDirectory(new File(path + "/enron1k"));
        }catch (Exception e)
        {
           throw new ServletException(e);
        }
    }


    public void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        String query = req.getParameter("Regex");
        IndexReader reader = null;
        try {
            out.println(" Results for regex["+ query+"]" +":");

            reader = IndexReader.open(index);
            IndexQuery indexQuery = new IndexQuery(reader);
            Set<Integer> set = indexQuery.getPrunedSet(query);

            out.print("Pruned set: ");
            if(set != null){
                for (Integer aSet : set) {
                    out.print(aSet + " ");
                }
                out.println("| " + set.size() + " docs in total");
            }else{
                out.println( " All docs are possible matches");
            }
        }
        catch (Exception e)
        {
            out.println(e);
        }
        finally
        {
            if(reader != null)
            {
                reader.close();
            }
        }
    }

    private void addDoc(IndexWriter w, String value) throws IOException {
        Document doc = new Document();
        doc.add(new Field("", value, Field.Store.YES, Field.Index.ANALYZED));
        w.addDocument(doc);
    }
}