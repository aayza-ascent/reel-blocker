import http from "http";
import fs from "fs";
import path from "path";

const PORT = 3001;

http.createServer((req, res) => {
  const url = new URL(req.url!, `http://localhost:${PORT}`);
  const platform = url.searchParams.get("platform") ?? "android";
  const rulesPath = path.join(__dirname, `../../detection-rules/rules/${platform}.json`);

  try {
    const body = fs.readFileSync(rulesPath, "utf8");
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(body);
  } catch {
    res.writeHead(404);
    res.end(JSON.stringify({ error: "not_found" }));
  }
}).listen(PORT, () => console.log(`Rule server running on http://localhost:${PORT}`));
