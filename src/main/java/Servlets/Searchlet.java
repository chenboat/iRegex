package Servlets;

import indexer.NGramAnalyzer;
import indexer.NGramTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import query.IndexQuery;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
            initRAMIndex();
        }catch (Exception e)
        {
           throw new ServletException(e);
        }
    }

    private void initRAMIndex() throws Exception{
        // Add documents to the index
        index = new RAMDirectory();
        NGramAnalyzer analyzer = new NGramAnalyzer(new NGramTokenizer(2));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,analyzer);

        IndexWriter w = new IndexWriter(index,config);
        addDoc(w, "hello");
        addDoc(w, "ting");
        addDoc(w, "test it");
        addDoc(w, "tim");
        w.close();
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