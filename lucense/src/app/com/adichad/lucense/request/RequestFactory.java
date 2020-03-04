/*******************************************************************************
 * RequestFactory.java
 * serves STATUS requests
 * ****************************************************************************/

package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import com.adichad.lucense.exception.UnknownRequestTypeException;
import com.adichad.lucense.resource.SearchResourceManager;

public class RequestFactory {

  public enum RequestType {
    REQ_PING,
    REQ_STATUS,
    REQ_SEARCH,
    REQ_REGWORKER,
    REQ_UNKNOWN,
    REQ_LOADINDEXES,
    REQ_UNLOADINDEXES,
    REQ_JANITOR,
    REQ_SPELLCORR,
    REQ_INVERTSTEMPLURAL,
    REQ_INITWRITER,
    REQ_REPLACEDOCUMENTS,
    REQ_CLOSEWRITER,
    REQ_DELETEDOCUMENTS,
    REQ_ANALYZEQUERY,
    REQ_TERMSTATS,
    REQ_ADDTOAUX,
    REQ_DELETEFROMAUX,
    REQ_OPTIMIZEAUX,
    REQ_RELOADCONF,
    REQ_UPDATEAUX_CELLWISE;

    public static RequestType getRequestType(byte b) {
      switch (b) {
      case 0:
        return RequestType.REQ_PING;
      case 1:
        return RequestType.REQ_STATUS;
      case 2:
        return RequestType.REQ_SEARCH;
      case 3:
        return RequestType.REQ_REGWORKER;
      case 4:
        return RequestType.REQ_LOADINDEXES;
      case 5:
        return RequestType.REQ_UNLOADINDEXES;
      case 6:
        return RequestType.REQ_JANITOR;
      case 7:
        return RequestType.REQ_SPELLCORR;
      case 8:
        return RequestType.REQ_INVERTSTEMPLURAL;
      case 9:
        return RequestType.REQ_INITWRITER;
      case 10:
        return RequestType.REQ_REPLACEDOCUMENTS;
      case 11:
        return RequestType.REQ_CLOSEWRITER;
      case 12:
        return RequestType.REQ_DELETEDOCUMENTS;
      case 13:
        return RequestType.REQ_ANALYZEQUERY;
      case 14:
        return RequestType.REQ_TERMSTATS;
      case 15:
        return RequestType.REQ_ADDTOAUX;
      case 16:
        return RequestType.REQ_DELETEFROMAUX;
      case 17:
        return RequestType.REQ_OPTIMIZEAUX;
      case 18:
        return RequestType.REQ_RELOADCONF;
      case 19:
        return RequestType.REQ_UPDATEAUX_CELLWISE;  
      default:
        return RequestType.REQ_UNKNOWN;
      }
    }

    public static RequestType getRequestType(String s) {
      s = s.toLowerCase();
      if (s.equals("ping"))
        return RequestType.REQ_PING;
      else if (s.equals("status"))
        return RequestType.REQ_STATUS;
      else if (s.equals("search"))
        return RequestType.REQ_SEARCH;
      else if (s.equals("regworker"))
        return RequestType.REQ_REGWORKER;
      else if (s.equals("loadindex"))
        return RequestType.REQ_LOADINDEXES;
      else if (s.equals("unloadindex"))
        return RequestType.REQ_UNLOADINDEXES;
      else if (s.equals("collectgarbage"))
        return RequestType.REQ_JANITOR;
      else if (s.equals("correctspellings"))
        return RequestType.REQ_SPELLCORR;
      else if (s.equals("invertstemplurals"))
        return RequestType.REQ_INVERTSTEMPLURAL;
      else if (s.equals("initwriter"))
        return RequestType.REQ_INITWRITER;
      else if (s.equals("replacedocs"))
        return RequestType.REQ_REPLACEDOCUMENTS;
      else if (s.equals("closewriter"))
        return RequestType.REQ_CLOSEWRITER;
      else if (s.equals("deletedocs"))
        return RequestType.REQ_DELETEDOCUMENTS;
      else if (s.equals("analyzequery"))
        return RequestType.REQ_ANALYZEQUERY;
      else if (s.equals("termstats"))
        return RequestType.REQ_TERMSTATS;
      else if (s.equals("addaux"))
        return RequestType.REQ_ADDTOAUX;
      else if (s.equals("deleteaux"))
        return RequestType.REQ_DELETEFROMAUX;
      else if (s.equals("optimizeaux"))
        return RequestType.REQ_OPTIMIZEAUX;
      else if (s.equals("reloadconf"))
        return RequestType.REQ_RELOADCONF;
      else if (s.equals("updateaux_cellwise"))
        return RequestType.REQ_UPDATEAUX_CELLWISE;
      return RequestType.REQ_PING;
    }
  }

  public static Request receiveRequest(Socket sock, SearchResourceManager context) throws IOException,
      UnknownRequestTypeException, EOFException {
    DataInputStream dis = new DataInputStream(sock.getInputStream());
    int version = dis.readInt();
    byte commandType = dis.readByte();
    RequestType type = RequestType.getRequestType(commandType);
    if (!context.isAuthorized(sock, type))
      return null;
    int id = dis.readInt();
    Request req = null;
    switch (type) {
    case REQ_PING:
      req = new PingRequest(sock, version, id);
      break;
    case REQ_STATUS:
      req = new StatusRequest(sock, version, id);
      break;
    case REQ_SEARCH:
      req = new SearchRequest(sock, version, id);
      break;
    case REQ_REGWORKER:
      req = new RegisterWorkerRequest(sock, version, id);
      break;
    case REQ_LOADINDEXES:
      req = new IndexLoadRequest(sock, version, id);
      break;
    case REQ_UNLOADINDEXES:
      req = new IndexUnloadRequest(sock, version, id);
      break;
    case REQ_JANITOR:
      req = new GarbageCollectRequest(sock, version, id);
      break;
    case REQ_SPELLCORR:
      req = new SpellingCorrectionRequest(sock, version, id);
      break;
    case REQ_INVERTSTEMPLURAL:
      req = new PluralStemInversionRequest(sock, version, id);
      break;
    case REQ_INITWRITER:
      req = new InitWriterRequest(sock, version, id);
      break;
    case REQ_REPLACEDOCUMENTS:
      req = new ReplaceDocumentsRequest(sock, version, id);
      break;
    case REQ_CLOSEWRITER:
      req = new CloseWriterRequest(sock, version, id);
      break;
    case REQ_DELETEDOCUMENTS:
      req = new DeleteDocumentsRequest(sock, version, id);
      break;
    case REQ_ANALYZEQUERY:
      req = new AnalyzeQueryRequest(sock, version, id);
      break;
    case REQ_TERMSTATS:
      req = new TermStatsRequest(sock, version, id);
      break;
    case REQ_ADDTOAUX:
      req = new AddToAuxRequest(sock, version, id, commandType);
      break;
    case REQ_DELETEFROMAUX:
      req = new DeleteFromAuxRequest(sock, version, id, commandType);
      break;
    case REQ_OPTIMIZEAUX:
      req = new OptimizeAuxRequest(sock, version, id, commandType);
      break;
    case REQ_RELOADCONF:
      req = new ReloadConfigRequest(sock, version, id);
      break;
    case REQ_UPDATEAUX_CELLWISE:
      req = new UpdateAuxCellWiseRequest(sock, version, id, commandType);
      break;
    case REQ_UNKNOWN:
      throw new UnknownRequestTypeException();
    }

    return req;
  }

  public static Request initRegisterWorkerRequest() {
    return new RegisterWorkerRequest(null, 1, 0);
  }
}
/*******************************************************************************
 * RequestFactory.java ENDS *
 ***************************************************************************/
