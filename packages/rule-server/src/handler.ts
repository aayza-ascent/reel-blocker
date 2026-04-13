import { S3Client, GetObjectCommand } from "@aws-sdk/client-s3";
import { APIGatewayProxyHandler } from "aws-lambda";

const s3 = new S3Client({ region: process.env.AWS_REGION });

export const handler: APIGatewayProxyHandler = async (event) => {
  const platform = event.queryStringParameters?.platform ?? "android";
  const clientVersion = event.queryStringParameters?.schemaVersion ?? "1";
  const key = `rules/latest/${platform}.json`;

  try {
    const obj = await s3.send(new GetObjectCommand({
      Bucket: process.env.RULES_BUCKET!,
      Key: key,
    }));
    const body = await obj.Body!.transformToString();
    const rules = JSON.parse(body);

    if (parseInt(clientVersion) < rules.schemaVersion) {
      return {
        statusCode: 422,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ error: "schema_version_too_old", minRequired: rules.schemaVersion }),
      };
    }

    return {
      statusCode: 200,
      headers: {
        "Content-Type": "application/json",
        "Cache-Control": "public, max-age=3600, stale-while-revalidate=86400",
      },
      body,
    };
  } catch (e) {
    return { statusCode: 500, body: JSON.stringify({ error: "rule_fetch_failed" }) };
  }
};
