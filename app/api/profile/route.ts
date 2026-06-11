import { forwardJsonRequest } from "@/lib/backend";

export function GET(request: Request) {
  return forwardJsonRequest(request, "/api/profile");
}

export function PUT(request: Request) {
  return forwardJsonRequest(request, "/api/profile");
}
