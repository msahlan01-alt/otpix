import express, { Request, Response } from "express";
import cors from "cors";
import { OpenRouter } from "@openrouter/sdk";

const app = express();
app.use(cors());
app.use(express.json());

const openrouter = new OpenRouter({
  apiKey:
    process.env.OPENROUTER_API_KEY ||
    "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",
});


app.post("/generate-image", async (req: Request, res: Response) => {
  const { prompt } = req.body;
  if (!prompt) return res.status(400).json({ error: "Prompt required" });

  
  res.writeHead(200, {
    "Content-Type": "text/event-stream",
    "Cache-Control": "no-cache",
    Connection: "keep-alive",
  });

  try {
    
    const stream = await openrouter.chat.stream({
      model: "x-ai/grok-imagine-image-quality",
      stream: true,
      messages: [{ role: "user", content: prompt }],
      modalities: ["image"],
    });

    
    for await (const chunk of stream) {
      
      const message = chunk.choices?.[0]?.message;
      if (message?.images) {
        for (const image of message.images) {
          const imageUrl = image.image_url.url;
          res.write(
            `data: ${JSON.stringify({ type: "image", url: imageUrl })}\n\n`,
          );
        }
      } else if (chunk.choices?.[0]?.delta?.content) {
        
        const text = chunk.choices[0].delta.content;
        res.write(
          `data: ${JSON.stringify({ type: "text", content: text })}\n\n`,
        );
      }
    }
    res.write(`data: ${JSON.stringify({ type: "done" })}\n\n`);
    res.end();
  } catch (err) {
    console.error(err);
    res.write(
      `data: ${JSON.stringify({ type: "error", message: "Generation failed" })}\n\n`,
    );
    res.end();
  }
});

const PORT = 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
