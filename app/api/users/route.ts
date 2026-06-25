import { forwardJsonRequest } from "@/lib/backend";

export function GET(request: Request) {
  return forwardJsonRequest(request, "/api/users");
}

export function POST(request: Request) {
  return forwardJsonRequest(request, "/api/users");
}
