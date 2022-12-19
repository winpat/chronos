{ pkgs ? import <nixpkgs> { } }:

pkgs.mkShell {
  buildInputs = [
    pkgs.clojure
    pkgs.docker-compose
    pkgs.postgresql
    pkgs.clj-kondo
    pkgs.httpie
    pkgs.nodejs
    pkgs.openjdk
  ];
}
